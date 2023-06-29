
import eu.europa.esig.dss.DomUtils;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.TimestampType;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.ers.xmlers.XMLEvidenceRecordFacade;
import eu.europa.esig.ers.xmlers.XMLEvidenceRecordUtils;

import eu.europa.esig.dss.model.TimestampBinary;
import eu.europa.esig.dss.validation.timestamp.TimestampToken;

import eu.europa.esig.ers.xmlers.jaxb.ArchiveTimeStampSequenceType;
import eu.europa.esig.ers.xmlers.jaxb.ArchiveTimeStampType;
import eu.europa.esig.ers.xmlers.jaxb.EvidenceRecordType;
import eu.europa.esig.ers.xmlers.jaxb.HashTreeType;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.tsp.TSPException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ValidateXMLERTest {

    private static XMLEvidenceRecordUtils xmlerUtils;

    @BeforeAll
    public static void init() {
        xmlerUtils = XMLEvidenceRecordUtils.getInstance();
    }


    @Test
    public void testXMLER() throws JAXBException, XMLStreamException, IOException, SAXException {
        marshallUnmarshall(new File("src/test/resources/ER_01.xml"));
    }

    @Test
    public void testVerifyER()throws IOException, NoSuchAlgorithmException, JAXBException, SAXException, XMLStreamException, CMSException, TSPException {
        boolean isValid = verifyERArchiveTimeStampMessageImprint(new File("src/test/resources/ER_01.xml"));
        assertEquals(isValid,true);

        isValid = verifyERArchiveTimeStampMessageImprint(new File("src/test/resources/ER_47.xml"));
        assertEquals(isValid,true);

        isValid = verifyERArchiveTimeStampMessageImprint(new File("src/test/resources/xmler_1.txt.xml"));
        assertEquals(isValid,true);

    }

    @Test
    public void testVerifyEREPres()throws IOException, NoSuchAlgorithmException, JAXBException, SAXException, XMLStreamException, CMSException, TSPException {
        boolean isValid = verifyERArchiveTimeStampMessageImprint(new File("src/test/resources/XMLER_EPRES/er-ao-c2e7c2e2-10ef-4497-bced-82ced6ce93a4.xml"));
        //assertEquals(isValid,true);

        isValid = verifyERArchiveTimeStampMessageImprint(new File("src/test/resources/XMLER_EPRES/er-group-item-42a89ce5-0983-4246-ad7b-a735504cf23c.xml"));
        assertEquals(isValid,true);
    }

    @Test
    public void testVerifyEPresER()throws IOException, NoSuchAlgorithmException, JAXBException, SAXException, XMLStreamException, CMSException, TSPException {
        boolean isValid = false;
        File dir = new File("src/test/resources/XMLER_EPRES/er");
        File[] directoryListing = dir.listFiles();
        List<File> validFiles = new ArrayList<File>();
        List<File> invalidFiles = new ArrayList<File>();
        for (File child : directoryListing) {
            isValid = verifyERArchiveTimeStampMessageImprint(child);
            if(isValid){
                validFiles.add(child);
            }
            else{
                invalidFiles.add(child);
            }
            System.out.println("File " + child.getName() + " : " + isValid);
        }
        System.out.println("====List of all valid Files====");
        for(File f : validFiles){
            System.out.println(f.getName());
        }
        System.out.println("====List of all invalid Files====");
        if(invalidFiles.size() == 0)
            System.out.println("No invalid files");
        else {
            for (File f : invalidFiles) {
                System.out.println(f.getName());
            }
            //assertEquals(isValid,true);
        }
    }

    @Test
    public void testVerifyUCLER()throws IOException, NoSuchAlgorithmException, JAXBException, SAXException, XMLStreamException, CMSException, TSPException {
        boolean isValid = false;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        File dir = new File("src/test/resources/XMLER_UCL_2600");
        File[] directoryListing = dir.listFiles();
        List<File> validFiles = new ArrayList<File>();
        List<File> invalidFiles = new ArrayList<File>();
        for (File child : directoryListing) {
            isValid = verifyERArchiveTimeStampMessageImprint(child);
            if(isValid){
                validFiles.add(child);
            }
            else{
                invalidFiles.add(child);
            }
            System.out.println("File " + child.getName() + " : " + isValid);
        }
        System.out.println("====List of all valid Files====");
        for(File f : validFiles){
            System.out.println(f.getName());
        }
        System.out.println("====List of all invalid Files====");
        for(File f : invalidFiles){
            System.out.println(f.getName());
        }
        //assertEquals(isValid,true);
    }

    private void marshallUnmarshall(File file) throws JAXBException, XMLStreamException, IOException, SAXException {
        XMLEvidenceRecordFacade facade = XMLEvidenceRecordFacade.newFacade();

        EvidenceRecordType evidenceRecordType = facade.unmarshall(file);
        assertNotNull(evidenceRecordType);

        String marshall = facade.marshall(evidenceRecordType, true);
        assertNotNull(marshall);
    }

    public byte[] computeParent(List<byte[]> list,MessageDigest md) throws IOException, NoSuchAlgorithmException {
        // If the list contains only one element, its parent is itself.
        if(list.size() == 1){
            return list.get(0);
        }
        byte[] result;

        list.sort(new ByteArrayComparator());

        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            for (byte[] byteArray : list) {
                baos.write(byteArray);
            }
            byte[] sorted_input_concatenated = baos.toByteArray();
            result = md.digest(sorted_input_concatenated);

            String encodeToString = Base64.getEncoder().encodeToString(result);
            System.out.println(encodeToString);
        }

        return result;
    }


    public byte[] getHashTreeRoot(HashTreeType hashTree, MessageDigest md) throws IOException, NoSuchAlgorithmException {
        System.out.println("=====Begin HashTree Root computation====");
        // A hashTree object has a list of Digest Sequences.
        // Each Digest Sequence corresponds to a set of node in the reduced hash tree.
        List<HashTreeType.Sequence> sequenceList = hashTree.getSequence();
        byte[] lastParent = null;
        for(HashTreeType.Sequence sequence : sequenceList){
            List<byte[]> hashList = sequence.getDigestValue();
            if(lastParent != null) {
                hashList.add(lastParent);
            }
            lastParent = computeParent(hashList,md);
        }

        System.out.println("=====End HashTree Root computation====");

        return lastParent;
    }


    public boolean verifyERArchiveTimeStampMessageImprint(File xmlER) throws IOException, NoSuchAlgorithmException, JAXBException, SAXException, XMLStreamException, CMSException, TSPException {
        XMLEvidenceRecordFacade facade = XMLEvidenceRecordFacade.newFacade();
        EvidenceRecordType evidenceRecordType = facade.unmarshall(xmlER);

        //Dom equivalent for XMLER
        Document ERdoc = DomUtils.buildDOM(new FileInputStream(xmlER));
        NodeList ATSSeqs = ERdoc.getElementsByTagName("ArchiveTimeStampSequence");


        // An XMLER only has one ArchiveTimesTampSequence, that is a list of ArchiveTimeStampChain
        List<ArchiveTimeStampSequenceType.ArchiveTimeStampChain> ATSCList = evidenceRecordType.getArchiveTimeStampSequence().getArchiveTimeStampChain();

        Element eATSSeq = (Element) ATSSeqs.item(0);
        NodeList ATSChains = eATSSeq.getElementsByTagName("ArchiveTimeStampChain");
        int i = 0;

        // An ArchiveTimeStampChain contains a list of ArchiveTimeStamp object.
        // Each ArchiveTimeStamp object encapsulates a reduced hash tree.
        // Each ArchiveTimeStamp object encapsulates an RFC3161 timestamp.
        // The messageImprint value of an RFC3161 timestamp in an ArchiveTimeStamp object must be match the value of the root of the corresponding reduced hash tree.
        for(ArchiveTimeStampSequenceType.ArchiveTimeStampChain ATSC : ATSCList){
            List<ArchiveTimeStampType> ATSList = ATSC.getArchiveTimeStamp();

            Element eATSChain = (Element) ATSChains.item(i);
            NodeList ATStamps = eATSChain.getElementsByTagName("ArchiveTimeStamp");
            int j = 0;

            byte[] hashValueToCompare = null;

            //An ArchiveTimeStampChain contains an URI identifying the digest algorithm used to build the (reduced) hash trees encapsulated in the ArchiveTimeStamp objects contained within the ArchiveTimesStampChain.
            //This digest algorithm is also the one that is used to generate the RFC 3161 timestamps encapsulated in those ArchiveTimeStamp objects.
            MessageDigest md = DigestAlgorithm.forXML(ATSC.getDigestMethod().getAlgorithm()).getMessageDigest();
            //An ArchiveTimeStampChain contains an URI identifying the canonicalization algorithm used to canonicalize the binary representation of the <TimeStamp> element in case of timestamp renewal.
            String ATSCCanonicalizationMethod = ATSC.getCanonicalizationMethod().getAlgorithm();
            // For each ArchiveTimeStamp object, the value root of the encapsulated reduced hash tree must match the value of the encapsulated RFC 3161 timestamp message imprint
            for(ArchiveTimeStampType ATS : ATSList){

                // The root of the hash tree must be computed based on the reduced hash tree encapsulated in the ArchiveTimeStamp element.
                HashTreeType hashTree = ATS.getHashTree();
                byte[] hashTreeRoot = getHashTreeRoot(hashTree, md);

                String timeStampValue = (String) ATS.getTimeStamp().getTimeStampToken().getContent().get(0);
                TimestampToken timestampToken = new TimestampToken(Base64.getDecoder().decode(timeStampValue), TimestampType.ARCHIVE_TIMESTAMP);
                byte[] messageImprint = timestampToken.getMessageImprint().getValue();
                System.out.println("Timestamp messageImprint value is: "+ Base64.getEncoder().encodeToString(messageImprint));

                //assertArrayEquals(hashTreeRoot,messageImprint);

                if (!Arrays.equals(messageImprint, hashTreeRoot)){
                    return false;
                }

                if (hashValueToCompare != null && !Arrays.equals(hashTree.getSequence().get(0).getDigestValue().get(0),hashValueToCompare)){
                    System.out.println(Base64.getEncoder().encodeToString(hashTree.getSequence().get(0).getDigestValue().get(0))+  " is different from " + Base64.getEncoder().encodeToString(hashValueToCompare));
                    // return false;
                }

                // The canonicalized TimeStamp element is used to compute the first element of the HashTree sub-element of the next ArchiveTimeStamp.
                Element eATS = (Element) ATStamps.item(j);
                NodeList TimeStamps = eATS.getElementsByTagName("TimeStamp");
                // ArchiveTimeStamp elements only have one TimeStamp element.
                Node TimeStamp = TimeStamps.item(0);
                byte[] canonicalizedTimeStamp = canonicalizeSubtree(ATSCCanonicalizationMethod,TimeStamp);
                byte[] canonicalizedTimeStampDigest = md.digest(canonicalizedTimeStamp);
                System.out.println("=====BEGIN TimeStamp Element Canonlicalization=====");
                System.out.println(new String(canonicalizedTimeStamp, StandardCharsets.UTF_8));
                System.out.println(Base64.getEncoder().encodeToString(canonicalizedTimeStampDigest));
                System.out.println("=====END TimeStamp Element Canonlicalization=====");
                hashValueToCompare = canonicalizedTimeStampDigest;
                j++;
            }
            i ++;
        }
        return true;
    }

    public static byte[] canonicalizeSubtree(final String canonicalizationMethod, final Node node) {
        org.apache.xml.security.Init.init();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final Canonicalizer c14n = Canonicalizer.getInstance(canonicalizationMethod);
            c14n.canonicalizeSubtree(node, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new DSSException("Cannot canonicalize the subtree", e);
        }
    }

    public class ByteArrayComparator implements Comparator<byte[]> {

        public int compare(byte[] left, byte[] right) {

            for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {

                int a = (left[i] & 0xff);

                int b = (right[j] & 0xff);

                if (a != b) {

                    return a - b;

                }

            }

            return left.length - right.length;

        }

    }


}
