/**
 * DSS - Digital Signature Services
 * Copyright (C) 2015 European Commission, provided under the CEF programme
 * 
 * This file is part of the "DSS - Digital Signature Services" project.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package eu.europa.esig.dss.xades.extension;

import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.exception.IllegalInputException;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.validation.reports.Reports;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XAdESExtensionInvalidLevelsTest extends AbstractXAdESTestExtension {

    private SignatureLevel originalSignatureLevel;
    private SignatureLevel finalSignatureLevel;

    @Test
    public void tLevelExtensionTest() throws Exception {
        originalSignatureLevel = SignatureLevel.XAdES_BASELINE_T;
        DSSDocument signedDocument = getSignedDocument(getOriginalDocument());
        Reports reports = verify(signedDocument);
        checkOriginalLevel(reports.getDiagnosticData());
        assertEquals(1, reports.getDiagnosticData().getTimestampList().size());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_B;
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> extendSignature(signedDocument));
        assertEquals("Unsupported signature format 'XAdES-BASELINE-B' for extension.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_T;
        DSSDocument extendedSignature = extendSignature(signedDocument);
        reports = verify(extendedSignature);
        checkFinalLevel(reports.getDiagnosticData());
        assertEquals(2, reports.getDiagnosticData().getTimestampList().size());
    }

    @Test
    public void ltLevelExtensionTest() throws Exception {
        originalSignatureLevel = SignatureLevel.XAdES_BASELINE_LT;
        DSSDocument signedDocument = getSignedDocument(getOriginalDocument());
        Reports reports = verify(signedDocument);
        checkOriginalLevel(reports.getDiagnosticData());
        assertEquals(1, reports.getDiagnosticData().getTimestampList().size());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_B;
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> extendSignature(signedDocument));
        assertEquals("Unsupported signature format 'XAdES-BASELINE-B' for extension.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_T;
        exception = assertThrows(IllegalInputException.class, () -> extendSignature(signedDocument));
        assertEquals("Cannot extend signature to 'XAdES-BASELINE-T'. The signature is already extended with LT level.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_LT;
        DSSDocument extendedSignature = extendSignature(signedDocument);
        reports = verify(extendedSignature);
        checkFinalLevel(reports.getDiagnosticData());
        assertEquals(1, reports.getDiagnosticData().getTimestampList().size());
    }

    @Test
    public void ltaLevelExtensionTest() throws Exception {
        originalSignatureLevel = SignatureLevel.XAdES_BASELINE_LTA;
        DSSDocument signedDocument = getSignedDocument(getOriginalDocument());
        Reports reports = verify(signedDocument);
        checkOriginalLevel(reports.getDiagnosticData());
        assertEquals(2, reports.getDiagnosticData().getTimestampList().size());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_B;
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> extendSignature(signedDocument));
        assertEquals("Unsupported signature format 'XAdES-BASELINE-B' for extension.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_T;
        exception = assertThrows(IllegalInputException.class, () -> extendSignature(signedDocument));
        assertEquals("Cannot extend signature to 'XAdES-BASELINE-T'. The signature is already extended with LT level.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_LT;
        exception = assertThrows(IllegalInputException.class, () -> extendSignature(signedDocument));
        assertEquals("Cannot extend signature to 'XAdES-BASELINE-LT'. The signature is already extended with LTA level.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_LTA;
        DSSDocument extendedSignature = extendSignature(signedDocument);
        reports = verify(extendedSignature);
        checkFinalLevel(reports.getDiagnosticData());
        assertEquals(3, reports.getDiagnosticData().getTimestampList().size());
    }

    @Test
    public void cLevelExtensionTest() throws Exception {
        originalSignatureLevel = SignatureLevel.XAdES_C;
        DSSDocument signedDocument = getSignedDocument(getOriginalDocument());
        Reports reports = verify(signedDocument);
        checkOriginalLevel(reports.getDiagnosticData());
        assertEquals(1, reports.getDiagnosticData().getTimestampList().size());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_B;
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> extendSignature(signedDocument));
        assertEquals("Unsupported signature format 'XAdES-BASELINE-B' for extension.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_T;
        exception = assertThrows(IllegalInputException.class, () -> extendSignature(signedDocument));
        assertEquals("Cannot extend signature to 'XAdES-BASELINE-T'. The signature is already extended with LT level.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_C;
        DSSDocument extendedSignature = extendSignature(signedDocument);
        reports = verify(extendedSignature);
        checkFinalLevel(reports.getDiagnosticData());
        assertEquals(1, reports.getDiagnosticData().getTimestampList().size());
    }

    @Test
    public void xLevelExtensionTest() throws Exception {
        originalSignatureLevel = SignatureLevel.XAdES_X;
        DSSDocument signedDocument = getSignedDocument(getOriginalDocument());
        Reports reports = verify(signedDocument);
        checkOriginalLevel(reports.getDiagnosticData());
        assertEquals(2, reports.getDiagnosticData().getTimestampList().size());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_B;
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> extendSignature(signedDocument));
        assertEquals("Unsupported signature format 'XAdES-BASELINE-B' for extension.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_T;
        exception = assertThrows(IllegalInputException.class, () -> extendSignature(signedDocument));
        assertEquals("Cannot extend signature to 'XAdES-BASELINE-T'. The signature is already extended with LT level.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_C;
        exception = assertThrows(IllegalInputException.class, () -> extendSignature(signedDocument));
        assertEquals("Cannot extend signature to 'XAdES-C'. The signature is already extended with higher level.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_X;
        DSSDocument extendedSignature = extendSignature(signedDocument);
        reports = verify(extendedSignature);
        checkFinalLevel(reports.getDiagnosticData());
        assertEquals(2, reports.getDiagnosticData().getTimestampList().size());
    }

    @Test
    public void xlLevelExtensionTest() throws Exception {
        originalSignatureLevel = SignatureLevel.XAdES_XL;
        DSSDocument signedDocument = getSignedDocument(getOriginalDocument());
        Reports reports = verify(signedDocument);
        checkOriginalLevel(reports.getDiagnosticData());
        assertEquals(2, reports.getDiagnosticData().getTimestampList().size());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_B;
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> extendSignature(signedDocument));
        assertEquals("Unsupported signature format 'XAdES-BASELINE-B' for extension.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_T;
        exception = assertThrows(IllegalInputException.class, () -> extendSignature(signedDocument));
        assertEquals("Cannot extend signature to 'XAdES-BASELINE-T'. The signature is already extended with LT level.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_C;
        exception = assertThrows(IllegalInputException.class, () -> extendSignature(signedDocument));
        assertEquals("Cannot extend signature to 'XAdES-C'. The signature is already extended with higher level.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_X;
        exception = assertThrows(IllegalInputException.class, () -> extendSignature(signedDocument));
        assertEquals("Cannot extend signature to 'XAdES-X'. The signature is already extended with higher level.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_XL;
        DSSDocument extendedSignature = extendSignature(signedDocument);
        reports = verify(extendedSignature);
        checkFinalLevel(reports.getDiagnosticData());
        assertEquals(2, reports.getDiagnosticData().getTimestampList().size());
    }

    @Test
    public void aLevelExtensionTest() throws Exception {
        originalSignatureLevel = SignatureLevel.XAdES_A;
        DSSDocument signedDocument = getSignedDocument(getOriginalDocument());
        Reports reports = verify(signedDocument);
        checkOriginalLevel(reports.getDiagnosticData());
        assertEquals(3, reports.getDiagnosticData().getTimestampList().size());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_B;
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> extendSignature(signedDocument));
        assertEquals("Unsupported signature format 'XAdES-BASELINE-B' for extension.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_BASELINE_T;
        exception = assertThrows(IllegalInputException.class, () -> extendSignature(signedDocument));
        assertEquals("Cannot extend signature to 'XAdES-BASELINE-T'. The signature is already extended with LT level.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_C;
        exception = assertThrows(IllegalInputException.class, () -> extendSignature(signedDocument));
        assertEquals("Cannot extend signature to 'XAdES-C'. The signature is already extended with higher level.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_X;
        exception = assertThrows(IllegalInputException.class, () -> extendSignature(signedDocument));
        assertEquals("Cannot extend signature to 'XAdES-X'. The signature is already extended with higher level.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_XL;
        exception = assertThrows(IllegalInputException.class, () -> extendSignature(signedDocument));
        assertEquals("Cannot extend signature to 'XAdES-XL'. The signature is already extended with higher level.", exception.getMessage());

        finalSignatureLevel = SignatureLevel.XAdES_A;
        DSSDocument extendedSignature = extendSignature(signedDocument);
        reports = verify(extendedSignature);
        checkFinalLevel(reports.getDiagnosticData());
        assertEquals(4, reports.getDiagnosticData().getTimestampList().size());
    }

    @Override
    protected SignatureLevel getOriginalSignatureLevel() {
        return originalSignatureLevel;
    }

    @Override
    protected SignatureLevel getFinalSignatureLevel() {
        return finalSignatureLevel;
    }

    @Override
    public void extendAndVerify() throws Exception {
        // do nothing
    }

    @Override
    protected void checkOrphanTokens(DiagnosticData diagnosticData) {
        // skip
    }

}
