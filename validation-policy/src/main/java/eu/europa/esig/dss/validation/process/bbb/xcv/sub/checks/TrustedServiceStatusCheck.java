package eu.europa.esig.dss.validation.process.bbb.xcv.sub.checks;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import eu.europa.esig.dss.jaxb.detailedreport.XmlSubXCV;
import eu.europa.esig.dss.jaxb.diagnostic.XmlServiceStatus;
import eu.europa.esig.dss.jaxb.diagnostic.XmlServiceStatusType;
import eu.europa.esig.dss.jaxb.diagnostic.XmlTrustedServiceProviderType;
import eu.europa.esig.dss.validation.AdditionalInfo;
import eu.europa.esig.dss.validation.MessageTag;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.policy.rules.SubIndication;
import eu.europa.esig.dss.validation.process.bbb.AbstractMultiValuesCheckItem;
import eu.europa.esig.dss.validation.reports.wrapper.CertificateWrapper;
import eu.europa.esig.dss.x509.CertificateSourceType;
import eu.europa.esig.jaxb.policy.MultiValuesConstraint;

public class TrustedServiceStatusCheck extends AbstractMultiValuesCheckItem<XmlSubXCV> {

	private final CertificateWrapper certificate;

	private String serviceStatusStr;

	public TrustedServiceStatusCheck(XmlSubXCV result, CertificateWrapper certificate, MultiValuesConstraint constraint) {
		super(result, constraint);
		this.certificate = certificate;
	}

	@Override
	protected boolean process() {
		String trustedSource = certificate.getLastChainCertificateSource();
		// do not include Trusted list
		if (CertificateSourceType.TRUSTED_STORE.name().equals(trustedSource)) {
			return true;
		}

		Date certificateValidFrom = certificate.getNotBefore();
		List<XmlTrustedServiceProviderType> tspList = certificate.getCertificateTSPService();
		for (XmlTrustedServiceProviderType trustedServiceProvider : tspList) {
			XmlServiceStatus serviceStatus = trustedServiceProvider.getServiceStatus();
			if (serviceStatus != null && CollectionUtils.isNotEmpty(serviceStatus.getStatusService())) {
				for (XmlServiceStatusType status : serviceStatus.getStatusService()) {
					Date statusStartDate = status.getStartDate();
					Date statusEndDate = status.getEndDate();
					// The issuing time of the certificate should be into the validity period of the associated service
					if (certificateValidFrom.after(statusStartDate) && ((statusEndDate == null) || certificateValidFrom.before(statusEndDate))) {
						serviceStatusStr = StringUtils.trim(status.getStatus());
						return processValueCheck(serviceStatusStr);
					}
				}
			}
		}
		return false;
	}

	@Override
	protected String getAdditionalInfo() {
		Object[] params = new Object[] { serviceStatusStr };
		return MessageFormat.format(AdditionalInfo.TRUSTED_SERVICE_STATUS, params);
	}

	@Override
	protected MessageTag getMessageTag() {
		return MessageTag.XCV_TSL_ESP;
	}

	@Override
	protected MessageTag getErrorMessageTag() {
		return MessageTag.XCV_TSL_ESP_ANS;
	}

	@Override
	protected Indication getFailedIndicationForConclusion() {
		return Indication.INDETERMINATE;
	}

	@Override
	protected SubIndication getFailedSubIndicationForConclusion() {
		return SubIndication.TRY_LATER;
	}

}
