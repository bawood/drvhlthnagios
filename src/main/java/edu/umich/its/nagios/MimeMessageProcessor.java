package edu.umich.its.nagios;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Header;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.nagios.NagiosConstants;

public class MimeMessageProcessor implements Processor {
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void process(Exchange exchange) throws Exception {
        Message inMessage = null;

        String inType = exchange.getIn().getClass().getSimpleName();
        log.debug("input message class: " + inType);

        if ("MailMessage".equalsIgnoreCase(inType)) {
            inMessage = exchange.getIn();
        } else if ("GenericFileMessage".equalsIgnoreCase(inType)) {
            Properties props = new Properties();

            Session mailSession = Session.getDefaultInstance(props);
            inMessage = exchange.getIn();
            MimeMessage mimeMessage = new MimeMessage(mailSession, inMessage.getBody(InputStream.class));
            inMessage.setBody(mimeMessage, MimeMessage.class);

            for (Enumeration<?> e = mimeMessage.getAllHeaders(); e.hasMoreElements();) {
                Header h = (Header)e.nextElement();
                inMessage.setHeader(h.getName(), h.getValue());
            }
        } else {
            log.error("unhandled input message type: " + inType);
            return;
        }

        // parse subject into Nagios appropriate values
        String[] info = inMessage.getHeader("Subject", String.class).split("'");
        if (info.length == 6) {
            inMessage.setHeader(NagiosConstants.SERVICE_NAME, info[1]);
            inMessage.setHeader(NagiosConstants.HOST_NAME, info[3].toLowerCase());
            inMessage.setHeader(NagiosConstants.LEVEL, info[5].toUpperCase());
        } else {
            log.error("Expected 6 values in message subject found " + info.length + " check the quoting, "
                      + " subject was: " + inMessage.getHeader("Subject", String.class));
        }
    }

}
