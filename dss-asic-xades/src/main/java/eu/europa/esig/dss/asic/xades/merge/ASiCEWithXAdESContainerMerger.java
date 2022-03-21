package eu.europa.esig.dss.asic.xades.merge;

import eu.europa.esig.dss.DomUtils;
import eu.europa.esig.dss.asic.common.ASiCContent;
import eu.europa.esig.dss.asic.common.ASiCUtils;
import eu.europa.esig.dss.asic.common.ZipUtils;
import eu.europa.esig.dss.asic.xades.signature.asice.ASiCEWithXAdESManifestBuilder;
import eu.europa.esig.dss.asic.xades.validation.ASiCEWithXAdESManifestParser;
import eu.europa.esig.dss.exception.IllegalInputException;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.AdvancedSignature;
import eu.europa.esig.dss.validation.ManifestEntry;
import eu.europa.esig.dss.validation.ManifestFile;
import eu.europa.esig.dss.xades.DSSXMLUtils;
import eu.europa.esig.dss.xades.validation.XAdESSignature;
import eu.europa.esig.dss.xades.validation.XMLDocumentValidator;
import org.apache.xml.security.signature.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is used to merge ASiC-E with XAdES containers.
 *
 */
public class ASiCEWithXAdESContainerMerger extends AbstractASiCWithXAdESContainerMerger {

    /**
     * Empty constructor
     */
    ASiCEWithXAdESContainerMerger() {
    }

    /**
     * This constructor is used to create an ASiC-E With XAdES container merger from provided container documents
     *
     * @param containers {@link DSSDocument}s representing containers to be merged
     */
    public ASiCEWithXAdESContainerMerger(DSSDocument... containers) {
        super(containers);
    }

    /**
     * This constructor is used to create an ASiC-E With XAdES from to given {@code ASiCContent}s
     *
     * @param asicContents {@link ASiCContent}s to be merged
     */
    public ASiCEWithXAdESContainerMerger(ASiCContent... asicContents) {
        super(asicContents);
    }

    @Override
    protected boolean isSupported(DSSDocument container) {
        return super.isSupported(container) && (!ASiCUtils.isASiCSContainer(container) || doesNotContainSignatures(container));
    }

    private boolean doesNotContainSignatures(DSSDocument container) {
        List<String> entryNames = ZipUtils.getInstance().extractEntryNames(container);
        return !ASiCUtils.filesContainSignatures(entryNames);
    }

    @Override
    protected boolean isSupported(ASiCContent asicContent) {
        return super.isSupported(asicContent) && (!ASiCUtils.isASiCSContainer(asicContent) || doesNotContainSignatures(asicContent));
    }

    private boolean doesNotContainSignatures(ASiCContent asicContent) {
        return Utils.isCollectionEmpty(asicContent.getSignatureDocuments());
    }

    @Override
    protected void ensureContainerContentAllowMerge() {
        if (Arrays.stream(asicContents).allMatch(asicContent -> Utils.isCollectionEmpty(asicContent.getSignatureDocuments()))) {
            return; // no signatures -> can merge
        }
        if (Arrays.stream(asicContents).anyMatch(asicContent -> Utils.isCollectionNotEmpty(asicContent.getTimestampDocuments()))) {
            throw new UnsupportedOperationException("Unable to merge two ASiC-E with XAdES containers. " +
                    "One of the containers contains a detached timestamp!");
        }
    }

    @Override
    protected void ensureSignaturesAllowMerge() {
        if (Arrays.stream(asicContents).filter(asicContent -> Utils.isCollectionNotEmpty(asicContent.getManifestDocuments())).count() <= 1 &&
                Arrays.stream(asicContents).filter(asicContent -> Utils.isCollectionNotEmpty(asicContent.getSignatureDocuments())).count() <= 1) {
            // no signatures in all containers except maximum one. Can merge.
            return;
        }

        List<String> documentsCoveredBySignatures = getCoveredDocumentNames();
        if (Arrays.stream(asicContents).anyMatch(asicContent -> doCoverManifest(documentsCoveredBySignatures)) &&
                !sameSignedDocuments()) {
            throw new UnsupportedOperationException("Unable to merge ASiC-E with XAdES containers. " +
                    "manifest.xml is signed and the signer data does not match between containers!");
        }

        List<String> signatureNames = getAllSignatureDocumentNames();
        if (isConflictBetweenSignatureDocumentNames(signatureNames)) {
            if (doCoverOtherSignatures(signatureNames, documentsCoveredBySignatures)) {
                throw new UnsupportedOperationException("Unable to merge ASiC-E with XAdES containers. " +
                        "A signature covers another signature file, while having same signature names in both containers!");
            }
            ensureDocumentNamesDiffer();
        }

        // Create a merged manifest.xml file
        DSSDocument newManifest = createNewManifest();
        for (ASiCContent asicContent : asicContents) {
            asicContent.setManifestDocuments(Collections.singletonList(newManifest));
        }
    }

    private List<String> getCoveredDocumentNames() {
        List<String> result = new ArrayList<>();
        for (ASiCContent asicContent : asicContents) {
            for (DSSDocument signatureDocument : asicContent.getSignatureDocuments()) {
                XMLDocumentValidator documentValidator = new XMLDocumentValidator(signatureDocument);
                for (AdvancedSignature signature : documentValidator.getSignatures()) {
                    XAdESSignature xadesSignature = (XAdESSignature) signature;
                    for (Reference reference : xadesSignature.getReferences()) {
                        String referenceURI = DSSXMLUtils.getReferenceURI(reference);
                        if (!DomUtils.startsFromHash(referenceURI) && !DomUtils.isXPointerQuery(referenceURI)) {
                            result.add(referenceURI);
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean doCoverManifest(List<String> documentNames) {
        return documentNames.contains(ASiCUtils.ASICE_METAINF_MANIFEST);
    }

    private boolean sameSignedDocuments() {
        Set<String> signedDocumentNames = null;
        for (ASiCContent asicContent : asicContents) {
            Set<String> currentSignedDocumentNames = new HashSet<>(DSSUtils.getDocumentNames(asicContent.getSignedDocuments()));
            if (signedDocumentNames == null) {
                signedDocumentNames = currentSignedDocumentNames;
            } else if (!signedDocumentNames.equals(currentSignedDocumentNames)) {
                return false;
            }
        }
        return true;
    }

    private List<String> getAllSignatureDocumentNames() {
        List<String> signatureDocumentNames = new ArrayList<>();
        for (ASiCContent asicContent : asicContents) {
            signatureDocumentNames.addAll(DSSUtils.getDocumentNames(asicContent.getSignatureDocuments()));
        }
        return signatureDocumentNames;
    }

    private boolean isConflictBetweenSignatureDocumentNames(List<String> signatureDocumentNames) {
        for (String signatureDocumentName : signatureDocumentNames) {
            if (Collections.frequency(signatureDocumentNames, signatureDocumentName) > 1) {
                return true;
            }
        }
        return false;
    }

    private boolean doCoverOtherSignatures(List<String> signatureNames, List<String> coveredDocumentNames) {
        for (String signature : signatureNames) {
            if (coveredDocumentNames.contains(signature)) {
                return true;
            }
        }
        return false;
    }

    private DSSDocument createNewManifest() {
        List<ManifestEntry> manifestEntries = new ArrayList<>();
        List<String> addedFileNames = new ArrayList<>();

        for (ASiCContent asicContent : asicContents) {
            List<DSSDocument> manifestDocuments = asicContent.getManifestDocuments();
            for (ManifestEntry entry : getManifestFileEntries(manifestDocuments)) {
                if (!addedFileNames.contains(entry.getFileName())) {
                    manifestEntries.add(entry);
                    addedFileNames.add(entry.getFileName());
                }
            }
            List<DSSDocument> signedDocuments = asicContent.getSignedDocuments();
            for (ManifestEntry entry : ASiCUtils.toSimpleManifestEntries(signedDocuments)) {
                if (!addedFileNames.contains(entry.getFileName())) {
                    manifestEntries.add(entry);
                    addedFileNames.add(entry.getFileName());
                }
            }
        }

        return createNewManifestXml(manifestEntries);
    }

    private List<ManifestEntry> getManifestFileEntries(List<DSSDocument> manifestDocuments) {
        if (Utils.isCollectionEmpty(manifestDocuments)) {
            return Collections.emptyList();

        } else if (Utils.collectionSize(manifestDocuments) > 1) {
            throw new IllegalInputException("One of the containers contain multiple manifest files!");

        } else {
            DSSDocument manifestDocument = manifestDocuments.get(0);
            if (!ASiCUtils.ASICE_METAINF_MANIFEST.equals(manifestDocument.getName())) {
                throw new IllegalInputException(String.format("A manifest file shall have a name '%s'.",
                        ASiCUtils.ASICE_METAINF_MANIFEST));
            }

            ASiCEWithXAdESManifestParser parser = new ASiCEWithXAdESManifestParser(manifestDocument);
            ManifestFile manifest = parser.getManifest();
            return manifest.getEntries();
        }
    }

    private DSSDocument createNewManifestXml(List<ManifestEntry> manifestEntries) {
        return new ASiCEWithXAdESManifestBuilder().setEntries(manifestEntries)
                .setManifestFilename(ASiCUtils.ASICE_METAINF_MANIFEST).build();
    }

    private void ensureDocumentNamesDiffer() {
        Set<String> usedSignatureNames = new HashSet<>();
        for (ASiCContent asicContent : asicContents) {
            for (DSSDocument signatureDocument : asicContent.getSignatureDocuments()) {
                if (usedSignatureNames.contains(signatureDocument.getName())) {
                    String newSignatureName = ASiCUtils.getNextAvailableASiCEWithXAdESSignatureName(usedSignatureNames);
                    signatureDocument.setName(newSignatureName);
                }
                usedSignatureNames.add(signatureDocument.getName());
            }
        }
    }

}
