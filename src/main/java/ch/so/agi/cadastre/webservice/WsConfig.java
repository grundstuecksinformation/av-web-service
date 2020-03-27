package ch.so.agi.cadastre.webservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class WsConfig {
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
    
    @Bean
    public Jaxb2Marshaller createMarshaller() {
        Jaxb2Marshaller marshaller=new Jaxb2Marshaller();
        //marshaller.setClassesToBeBound(ch.ehi.oereb.schemas.oereb._1_0.versioning.ObjectFactory.class,ch.ehi.oereb.schemas.oereb._1_0.extract.ObjectFactory.class);
        marshaller.setPackagesToScan("ch.so.geo.schema");
        marshaller.setSupportJaxbElementClass(true);
        marshaller.setLazyInit(true);
        return marshaller;
    }
}
