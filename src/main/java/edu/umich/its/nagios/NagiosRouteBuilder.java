package edu.umich.its.nagios;

import org.apache.camel.builder.RouteBuilder;

public class NagiosRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("{{fakemail.inEndpoint}}").
        log("Processing ${file:absolute.path} ").
        to("mimeProcessor").
        log("Processed file with Message-ID ${in.header.Message-ID}: for driver " +
        		"\"${in.header.CamelNagiosServiceName}\" " +
        		"on server ${in.header.CamelNagiosHostName} " +
        		"at level ${in.header.CamelNagiosLevel}").
        to("nagios:{{nagiosEndpoint}}");

        from("{{prod.inEndpoint}}").
        log("Processing mail input with Message-ID ${in.header.Message-ID}").
        to("mimeProcessor").
        log("Processed mail input with Message-ID ${in.header.Message-ID}: for driver " +
                        "\"${in.header.CamelNagiosServiceName}\" " +
                        "on server ${in.header.CamelNagiosHostName} " +
                        "at level ${in.header.CamelNagiosLevel}").
        to("nagios:{{nagiosEndpoint}}");

        from("servlet:///hello").transform(constant("Hello World!"));
    }

}
