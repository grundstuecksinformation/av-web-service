package ch.so.agi.cadastre.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.so.geo.schema.agi.cadastre._0_9.extract.GetEGRIDResponse;
import ch.so.geo.schema.agi.cadastre._0_9.extract.RealEstateType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.WKBWriter;
import org.slf4j.Logger;

//@RestController
@Controller
public class MainController {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private static final String PARAM_FORMAT_PDF = "pdf";
    private static final String PARAM_FORMAT_XML = "xml";

    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LIEGENSCHAFT = "dm01vch24lv95dliegenschaften_liegenschaft";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_SELBSTRECHT = "dm01vch24lv95dliegenschaften_selbstrecht";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_BERGWERK = "dm01vch24lv95dliegenschaften_bergwerk";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_GRUNDSTUECK = "dm01vch24lv95dliegenschaften_grundstueck";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LSNACHFUEHRUNG = "dm01vch24lv95dliegenschaften_lsnachfuehrung";
    private static final String TABLE_DM01VCH24LV95DGEMEINDEGRENZEN_GEMEINDE = "dm01vch24lv95dgemeindegrenzen_gemeinde";  
    private static final String TABLE_DM01VCH24LV95DBODENBEDECKUNG_BOFLAECHE  = "dm01vch24lv95dbodenbedeckung_boflaeche"; 
    private static final String TABLE_DM01VCH24LV95DBODENBEDECKUNG_PROJBOFLAECHE  = "dm01vch24lv95dbodenbedeckung_projboflaeche"; 
    private static final String TABLE_DM01VCH24LV95DBODENBEDECKUNG_GEBAEUDENUMMER = "dm01vch24lv95dbodenbedeckung_gebaeudenummer";
    private static final String TABLE_DM01VCH24LV95DBODENBEDECKUNG_PROJGEBAEUDENUMMER = "dm01vch24lv95dbodenbedeckung_projgebaeudenummer";
    private static final String TABLE_DM01VCH24LV95DEINZELOBJEKTE_EINZELOBJEKT  = "dm01vch24lv95deinzelobjekte_einzelobjekt"; 
    private static final String TABLE_DM01VCH24LV95DEINZELOBJEKTE_FLAECHENELEMENT  = "dm01vch24lv95deinzelobjekte_flaechenelement"; 
    private static final String TABLE_DM01VCH24LV95DEINZELOBJEKTE_OBJEKTNUMMER  = "dm01vch24lv95deinzelobjekte_objektnummer"; 
    private static final String TABLE_DM01VCH24LV95NOMENKLATUR_FLURNAME = "dm01vch24lv95dnomenklatur_flurname";
    private static final String TABLE_PLZOCH1LV95DPLZORTSCHAFT_PLZ6 = "plzoch1lv95dplzortschaft_plz6";
    private static final String TABLE_PLZOCH1LV95DPLZORTSCHAFT_ORTSCHAFT = "plzoch1lv95dplzortschaft_ortschaft";
    private static final String TABLE_PLZOCH1LV95DPLZORTSCHAFT_ORTSCHAFTSNAME = "plzoch1lv95dplzortschaft_ortschaftsname";
    private static final String TABLE_DM01VCH24LV95DGEBAEUDEADRESSEN_GEBAEUDEEINGANG = "dm01vch24lv95dgebaeudeadressen_gebaeudeeingang";
    private static final String TABLE_DM01VCH24LV95DGEBAEUDEADRESSEN_LOKALISATIONSNAME = "dm01vch24lv95dgebaeudeadressen_lokalisationsname"; 
    private static final String TABLE_SO_G_V_0180822GRUNDBUCHKREISE_GRUNDBUCHKREIS = "so_g_v_0180822grundbuchkreise_grundbuchkreis";

    protected static final String extractNS = "http://geo.so.ch/schema/AGI/Cadastre/0.9/Extract";

    @Autowired
    Jaxb2Marshaller marshaller;

    @Autowired
    JdbcTemplate jdbcTemplate;
    
    @Autowired
    NamedParameterJdbcTemplate jdbcParamTemplate; 

    @Value("${cadastre.dbschema}")
    private String dbschema;
    
    @Value("${cadastre.minIntersection:1}")
    private double minIntersection;

    @GetMapping("/")
    public ResponseEntity<String>  ping() {
        return new ResponseEntity<String>("cadastre web service",HttpStatus.OK);
    }
    
    @GetMapping("/getegrid/{format}/{identdn:[a-zA-Z].{2,11}}/{number}")
    public ResponseEntity<GetEGRIDResponse> getEgridByNumber(@PathVariable String format, @PathVariable String identdn,
            @PathVariable String number) {
        if (!format.equals(PARAM_FORMAT_XML)) {
            throw new IllegalArgumentException("unsupported format <" + format + ">");
        }
        GetEGRIDResponse ret = new GetEGRIDResponse();
        List<JAXBElement<?>[]> gsList=jdbcTemplate.query(
                "SELECT egris_egrid,nummer,g.nbident,g.art,TO_CHAR(nf.gueltigereintrag, 'yyyy-mm-dd') gueltigereintrag,TO_CHAR(nf.gbeintrag, 'yyyy-mm-dd') gbeintrag,ST_AsText(geometrie) FROM " +getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LSNACHFUEHRUNG + " nf"
                        + " LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_GRUNDSTUECK+" g ON g.entstehung = nf.t_id"
                        +" LEFT JOIN (SELECT liegenschaft_von as von, geometrie FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LIEGENSCHAFT
                         +" UNION ALL SELECT selbstrecht_von as von,  geometrie FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_SELBSTRECHT
                         +" UNION ALL SELECT bergwerk_von as von,     geometrie FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_BERGWERK+") b ON b.von=g.t_id WHERE g.nummer=? AND g.nbident=?"
                 , new RowMapper<JAXBElement<?>[]>() {
                    @Override
                    public JAXBElement<?>[] mapRow(ResultSet rs, int rowNum) throws SQLException {
                        JAXBElement<?> ret[]=new JAXBElement[6];
                        ret[0]=new JAXBElement<String>(new QName(extractNS,"Egrid"),String.class,rs.getString(1));
                        ret[1]=new JAXBElement<String>(new QName(extractNS,"Number"),String.class,rs.getString(2));
                        ret[2]=new JAXBElement<String>(new QName(extractNS,"IdentND"),String.class,rs.getString(3));
                        ret[3]=new JAXBElement<RealEstateType>(new QName(extractNS,"Type"),RealEstateType.class,gsArtLookUp(rs.getString(4)));
                        String sqlDate = (rs.getString(6) != null) ? rs.getString(6) : rs.getString(5);
                        ret[4]=new JAXBElement<XMLGregorianCalendar>(new QName(extractNS,"StateOf"),XMLGregorianCalendar.class,stringDateToXmlGregorianCalendar(sqlDate));
                        ret[5]=new JAXBElement<String>(new QName(extractNS,"Limit"),String.class,rs.getString(7));
                        return ret;
                    }
                }, number, identdn);

        for (JAXBElement<?>[] gs : gsList) {
            ret.getEgridsAndLimitsAndStateOves().add(gs[0]);
            ret.getEgridsAndLimitsAndStateOves().add(gs[1]);
            ret.getEgridsAndLimitsAndStateOves().add(gs[2]);
            ret.getEgridsAndLimitsAndStateOves().add(gs[3]);            
            ret.getEgridsAndLimitsAndStateOves().add(gs[4]);
            ret.getEgridsAndLimitsAndStateOves().add(gs[5]);
        }
        return new ResponseEntity<GetEGRIDResponse>(ret,gsList.size()>0?HttpStatus.OK:HttpStatus.NO_CONTENT);
    }

    /*
    @Operation(summary = "Find egrid by XY", description = "Liste der Grundstücke", tags = { "contact_egrid_egrid_wasn_da? (tags)" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK, Antwort konnte erstellt werden", content = @Content(schema = @Schema(implementation = GetEGRIDResponse.class))),
        @ApiResponse(responseCode = "204", description = "Kein Grundstück gefunden"), 
        @ApiResponse(responseCode = "500", description = "Andere Fehler") 
    })
    */
    @GetMapping(value="/getegrid/{format}", produces=MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<GetEGRIDResponse> getEgridByXY(@PathVariable String format,
            @RequestParam(value = "XY", required = false) String xy,
            @RequestParam(value = "GNSS", required = false) String gnss) {
        if (!format.equals(PARAM_FORMAT_XML)) {
            throw new IllegalArgumentException("unsupported format <" + format + ">");
        }
        if (xy == null && gnss == null) {
            throw new IllegalArgumentException("parameter XY or GNSS required");
        } else if (xy != null && gnss != null) {
            throw new IllegalArgumentException("only one of parameters XY or GNSS is allowed");
        }
        Coordinate coord = null;
        int srid = 2056;
        double scale = 1000.0;
        if (xy != null) {
            coord = parseCoord(xy);
            srid = 2056;
            if (coord.x < 2000000.0) {
                srid = 21781;
            }
        } else {
            coord = parseCoord(gnss);
            srid = 4326;
            scale = 100000.0;
        }

        WKBWriter geomEncoder = new WKBWriter(2, ByteOrderValues.BIG_ENDIAN, true);
        PrecisionModel precisionModel = new PrecisionModel(scale);
        GeometryFactory geomFact = new GeometryFactory(precisionModel, srid);
        byte geom[] = geomEncoder.write(geomFact.createPoint(coord));
        GetEGRIDResponse ret = new GetEGRIDResponse();

        List<JAXBElement<?>[]> gsList=jdbcTemplate.query(
                "SELECT egris_egrid,nummer,g.nbident,g.art,TO_CHAR(nf.gueltigereintrag, 'yyyy-mm-dd') gueltigereintrag,TO_CHAR(nf.gbeintrag, 'yyyy-mm-dd') gbeintrag,ST_AsText(geometrie) FROM " +getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LSNACHFUEHRUNG + " nf"
                        + " LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_GRUNDSTUECK+" g ON g.entstehung = nf.t_id"
                        +" LEFT JOIN (SELECT liegenschaft_von as von, geometrie FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LIEGENSCHAFT
                         +" UNION ALL SELECT selbstrecht_von as von,  geometrie FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_SELBSTRECHT
                         +" UNION ALL SELECT bergwerk_von as von,     geometrie FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_BERGWERK+") b ON b.von=g.t_id WHERE ST_DWithin(ST_Transform(?,2056),b.geometrie,1.0)"
                , new RowMapper<JAXBElement<?>[]>() {
                    @Override
                    public JAXBElement<?>[] mapRow(ResultSet rs, int rowNum) throws SQLException {
                        JAXBElement<?> ret[]=new JAXBElement[6];
                        ret[0]=new JAXBElement<String>(new QName(extractNS,"Egrid"),String.class,rs.getString(1));
                        ret[1]=new JAXBElement<String>(new QName(extractNS,"Number"),String.class,rs.getString(2));
                        ret[2]=new JAXBElement<String>(new QName(extractNS,"IdentND"),String.class,rs.getString(3));
                        ret[3]=new JAXBElement<RealEstateType>(new QName(extractNS,"Type"),RealEstateType.class,gsArtLookUp(rs.getString(4)));
                        String sqlDate = (rs.getString(6) != null) ? rs.getString(6) : rs.getString(5);
                        ret[4]=new JAXBElement<XMLGregorianCalendar>(new QName(extractNS,"StateOf"),XMLGregorianCalendar.class,stringDateToXmlGregorianCalendar(sqlDate));
                        ret[5]=new JAXBElement<String>(new QName(extractNS,"Limit"),String.class,rs.getString(7));
                        return ret;
                    }
                }, geom);

        for (JAXBElement<?>[] gs : gsList) {
            ret.getEgridsAndLimitsAndStateOves().add(gs[0]);
            ret.getEgridsAndLimitsAndStateOves().add(gs[1]);
            ret.getEgridsAndLimitsAndStateOves().add(gs[2]);
            ret.getEgridsAndLimitsAndStateOves().add(gs[3]);            
            ret.getEgridsAndLimitsAndStateOves().add(gs[4]);
            ret.getEgridsAndLimitsAndStateOves().add(gs[5]);
        }
        return new ResponseEntity<GetEGRIDResponse>(ret,gsList.size()>0?HttpStatus.OK:HttpStatus.NO_CONTENT);
    }
    
    private Coordinate parseCoord(String xy) {
        int sepPos = xy.indexOf(',');
        double x = Double.parseDouble(xy.substring(0, sepPos));
        double y = Double.parseDouble(xy.substring(sepPos + 1));
        Coordinate coord = new Coordinate(x, y);
        return coord;
    }
    
    private RealEstateType gsArtLookUp(String gsArt) {
        if("Liegenschaft".equals(gsArt)) {
            return RealEstateType.REAL_ESTATE;
        }else if("SelbstRecht.Baurecht".equals(gsArt)) {
            return RealEstateType.DISTINCT_AND_PERMANENT_RIGHTS_BUILDING_RIGHT;
        }else if("SelbstRecht.Quellenrecht".equals(gsArt)) {
            return RealEstateType.DISTINCT_AND_PERMANENT_RIGHTS_RIGHT_TO_SPRING_WATER;
        }else if("SelbstRecht.Konzessionsrecht".equals(gsArt)) {
            return RealEstateType.DISTINCT_AND_PERMANENT_RIGHTS_CONCESSION;
        }else if("Bergwerk".equals(gsArt)) {
            return RealEstateType.MINERAL_RIGHTS;
        }else {
            throw new IllegalStateException("unknown gsArt");
        }        
    }
    
    private XMLGregorianCalendar stringDateToXmlGregorianCalendar(String sqlDate) {
        XMLGregorianCalendar stateOf = null;
        GregorianCalendar gdate = new GregorianCalendar();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = formatter.parse(sqlDate);                            
            gdate.setTime(date);
            stateOf = DatatypeFactory.newInstance().newXMLGregorianCalendar(gdate);
            return stateOf;
        } catch (ParseException | DatatypeConfigurationException e ) {
            e.printStackTrace();                            
            logger.error(e.getMessage());
            throw new IllegalStateException(e);
        } 
    }
    
    private String getSchema() {
        return dbschema!=null?dbschema:"xcadastre";
    }    
}
