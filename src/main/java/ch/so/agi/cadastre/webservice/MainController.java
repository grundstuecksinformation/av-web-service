package ch.so.agi.cadastre.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.so.geo.schema.agi.cadastre._0_9.extract.AddressType;
import ch.so.geo.schema.agi.cadastre._0_9.extract.BuildingEntryType;
import ch.so.geo.schema.agi.cadastre._0_9.extract.BuildingType;
import ch.so.geo.schema.agi.cadastre._0_9.extract.Extract;
import ch.so.geo.schema.agi.cadastre._0_9.extract.GetEGRIDResponse;
import ch.so.geo.schema.agi.cadastre._0_9.extract.GetExtractByIdResponse;
import ch.so.geo.schema.agi.cadastre._0_9.extract.LCType;
import ch.so.geo.schema.agi.cadastre._0_9.extract.LandCoverShareType;
import ch.so.geo.schema.agi.cadastre._0_9.extract.LocalNameType;
import ch.so.geo.schema.agi.cadastre._0_9.extract.OrganisationType;
import ch.so.geo.schema.agi.cadastre._0_9.extract.PersonAddressType;
import ch.so.geo.schema.agi.cadastre._0_9.extract.RealEstateDPR;
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
import java.util.Base64;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.WKBReader;
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
    private static final String TABLE_SO_G_V_0180822NACHFUEHRUNGSKREISE_GEMEINDE = "so_g_v_0180822nachfuehrngskrise_gemeinde";
    
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
    
	@GetMapping(value = "/extract/{format}/geometry/{egrid}", consumes = MediaType.ALL_VALUE, produces = {
			MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<?> getExtractWithGeometryByEgrid(@PathVariable String format, @PathVariable String egrid) {
		if (!format.equals(PARAM_FORMAT_XML) && !format.equals(PARAM_FORMAT_PDF)) {
			throw new IllegalArgumentException("unsupported format <" + format + ">");
		}

		Grundstueck parcel = getParcelByEgrid(egrid);
		if (parcel == null) {
			return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
		}

		Extract extract	= new Extract();
		
		XMLGregorianCalendar today = null;
		try {
			logger.debug("timezone id {}", TimeZone.getDefault().getID());
			GregorianCalendar gdate = new GregorianCalendar();
			gdate.setTime(new java.util.Date());
			today = DatatypeFactory.newInstance().newXMLGregorianCalendar(gdate);
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException(e);
		}
        extract.setCreationDate(today);

        String base64String = "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";
        byte[] byteArray = Base64.getDecoder().decode(base64String);
        extract.setCantonalLogo(byteArray);
        
        OrganisationType responsibleOffice = new OrganisationType();
        responsibleOffice.setName("Amt für Geoinformation");
        responsibleOffice.setEmail("agi@bd.so.ch");
        responsibleOffice.setWeb("https://agi.so.ch");
        responsibleOffice.setPhone("032 627 75 92");
        AddressType responsibleOfficeAddress = new AddressType();
        responsibleOfficeAddress.setStreet("Rötistrasse");
        responsibleOfficeAddress.setNumber("4");
        responsibleOfficeAddress.setPostalCode(Integer.valueOf("4500"));
        responsibleOfficeAddress.setCity("Solothurn");
        responsibleOffice.setAddress(responsibleOfficeAddress);
        extract.setResponsibleOffice(responsibleOffice);
        
        RealEstateDPR realEstate = new RealEstateDPR();
        realEstate.setEGRID(egrid);
        realEstate.setIdentND(parcel.getNbident());
        realEstate.setNumber(parcel.getNummer());
        realEstate.setSubunitOfLandRegister(parcel.getGbSubKreis());
        realEstate.setMunicipality(parcel.getGemeinde());
        realEstate.setLimit(parcel.getGeometrie().toString());
        realEstate.setLandRegistryArea(new Double(parcel.getFlaechenmass()).intValue());
        realEstate.setType(gsArtLookUp(parcel.getArt()).value());
        
        setFlurnamen(realEstate, parcel.getGeometrie());
        setBodenbedeckung(realEstate, parcel.getGeometrie());
        setGebaeude(realEstate, parcel.getGeometrie());
        setNfGeometerAddress(realEstate, parcel.getBfsnr());
        setGrundbuchamtAddress(realEstate, parcel.getGbSubKreisNummer());
        setVermessungsaufsichtAddress(realEstate);
        
        extract.setRealEstate(realEstate);
        
		GetExtractByIdResponse response = new GetExtractByIdResponse();
		response.setExtract(extract);
		
        return new ResponseEntity<GetExtractByIdResponse>(response, HttpStatus.OK);
	}
	
	private void setVermessungsaufsichtAddress(RealEstateDPR realEstate) {       
        OrganisationType organisation = new OrganisationType();
        organisation.setName("Amt für Geoinformation");
        organisation.setEmail("agi@bd.so.ch");
        organisation.setWeb("https://agi.so.ch");
        organisation.setPhone("032 627 75 92");
        AddressType address = new AddressType();
        address.setStreet("Rötistrasse");
        address.setNumber("4");
        address.setPostalCode(Integer.valueOf("4500"));
        address.setCity("Solothurn");
        organisation.setAddress(address);
        realEstate.setSupervisionOffice(organisation);
	}
	
	private void setGrundbuchamtAddress(RealEstateDPR realEstate, int fosnr) {
		String stmt = "SELECT amt, amtschreiberei, strasse, hausnummer, plz, ortschaft, telefon, email, web, bfsnr FROM "+getSchema()+"."+TABLE_SO_G_V_0180822GRUNDBUCHKREISE_GRUNDBUCHKREIS
				+ " WHERE grundbuchkreis_bfsnr = ?";
        Map<String,Object> orgMap = jdbcTemplate.queryForMap(stmt, fosnr);
        OrganisationType organisation = new OrganisationType();
        organisation.setName((String) orgMap.get("amtschreiberei")); 
        if (orgMap.get("amt") != null) organisation.setLine1((String) orgMap.get("amt"));
        AddressType address = new AddressType();
        address.setStreet((String) orgMap.get("strasse"));
        if(orgMap.get("hausnummer") != null) address.setNumber((String) orgMap.get("hausnummer"));
        address.setPostalCode((Integer) orgMap.get("plz"));
        address.setCity((String) orgMap.get("ortschaft"));
        organisation.setAddress(address);
        organisation.setPhone((String) orgMap.get("telefon"));
        organisation.setEmail((String) orgMap.get("email"));
        organisation.setWeb((String)orgMap.get("web"));
        realEstate.setLandRegisterOffice(organisation);
	}
	
	private void setNfGeometerAddress(RealEstateDPR realEstate, int fosnr) {
		try {
			String stmt = "SELECT nfg_titel, nfg_name, nfg_vorname, firma, firma_zusatz, strasse, hausnummer, plz, ortschaft, telefon, web, email FROM "+getSchema()+"."+TABLE_SO_G_V_0180822NACHFUEHRUNGSKREISE_GEMEINDE
					+ " WHERE bfsnr = ?";
	        Map<String,Object> orgMap = jdbcTemplate.queryForMap(stmt, fosnr);
	        OrganisationType organisation = new OrganisationType();
	        organisation.setName((String) orgMap.get("firma")); 
	        if (orgMap.get("firma_zusatz") != null) organisation.setLine1((String) orgMap.get("firma_zusatz"));
	        PersonAddressType person = new PersonAddressType();
	        person.setLastName((String) orgMap.get("nfg_name"));
	        person.setFirstName((String) orgMap.get("nfg_vorname"));
	        organisation.setPerson(person);
	        AddressType address = new AddressType();
	        address.setStreet((String) orgMap.get("strasse"));
	        address.setNumber((String) orgMap.get("hausnummer"));
	        address.setPostalCode((Integer) orgMap.get("plz"));
	        address.setCity((String) orgMap.get("ortschaft"));
	        organisation.setAddress(address);
	        organisation.setPhone((String) orgMap.get("telefon"));
	        organisation.setEmail((String) orgMap.get("email"));
	        organisation.setWeb((String)orgMap.get("web"));
	        realEstate.setSurveyorOffice(organisation);
		} catch(EmptyResultDataAccessException ex) {
            logger.warn("no nfkreis for fosnr {}", fosnr);
        }
	}
	
    private void setGebaeude(RealEstateDPR realEstate, Geometry parcelGeom) {
        WKBWriter geomEncoder = new WKBWriter(2, ByteOrderValues.BIG_ENDIAN);
        byte parcelWkbGeometry[] = geomEncoder.write(parcelGeom);

        PrecisionModel precisionModel = new PrecisionModel(1000.0);
        GeometryFactory geomFactory = new GeometryFactory(precisionModel);

//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("geom", wkbGeometry);
        
        // DICTINCT ON ist eigentlich unnötig, da fachlich nur eine 1:1-Beziehung erlaubt ist.
        String stmt = "SELECT DISTINCT ON (bb.t_id) bb.t_id, ST_AsBinary(bb.geometrie) as geometrie, gwr_egid, 'realisiert' AS status, bb.art \n" + 
                "FROM \n" +
                "     "+getSchema()+"."+TABLE_DM01VCH24LV95DBODENBEDECKUNG_BOFLAECHE+" AS bb \n" + 
                "     LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DBODENBEDECKUNG_GEBAEUDENUMMER+" AS bbnr ON bbnr.gebaeudenummer_von = bb.t_id\n" + 
                "WHERE art = 'Gebaeude' AND ST_DWithin(ST_GeomFromWKB(?,2056), bb.geometrie, 0.1) \n" +
                "UNION ALL \n" +
                "SELECT DISTINCT ON (bb.t_id) bb.t_id, ST_AsBinary(bb.geometrie) as geometrie, gwr_egid, 'projektiert' AS status, bb.art \n" + 
                "FROM \n" +
                "     "+getSchema()+"."+TABLE_DM01VCH24LV95DBODENBEDECKUNG_PROJBOFLAECHE+" AS bb \n" + 
                "     LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DBODENBEDECKUNG_PROJGEBAEUDENUMMER+" AS bbnr ON bbnr.projgebaeudenummer_von = bb.t_id\n" + 
                "WHERE art = 'Gebaeude' AND ST_DWithin(ST_GeomFromWKB(?,2056), bb.geometrie, 0.1) \n" + 
                "UNION ALL \n" +
                "SELECT DISTINCT ON (fl.t_id) fl.t_id, ST_AsBinary(fl.geometrie) AS geometrie, eonr.gwr_egid, 'realisiert' AS status, eo.art \n" + 
                "FROM \n" + 
                "    (SELECT * FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DEINZELOBJEKTE_EINZELOBJEKT+" WHERE art = 'unterirdisches_Gebaeude') AS eo \n" + 
                "    JOIN (SELECT * FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DEINZELOBJEKTE_FLAECHENELEMENT+" WHERE ST_DWithin(ST_GeomFromWKB(?,2056), geometrie, 0.1)) AS fl \n" + 
                "    ON fl.flaechenelement_von = eo.t_id \n" + 
                "    LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DEINZELOBJEKTE_OBJEKTNUMMER+" AS eonr\n" + 
                "    ON eonr.objektnummer_von = eo.t_id";
               
        List<BuildingType> gebaeudeList = jdbcTemplate.query(stmt, new RowMapper<BuildingType>() {
            WKBReader decoder=new WKBReader(geomFactory);

            @Override
            public BuildingType mapRow(ResultSet rs, int rowNum) throws SQLException {
                logger.debug("bb t_id: " + rs.getString("t_id"));
                
                Geometry gebaeudeGeometry = null;
                try {
                    gebaeudeGeometry = decoder.read(rs.getBytes("geometrie"));
                }  catch (org.locationtech.jts.io.ParseException e) {
                    throw new IllegalStateException(e);
                }
                String bb_egid = rs.getString("gwr_egid");
                String status = rs.getString("status");
                String art = rs.getString("art");
                
                BuildingType gebaeude = new BuildingType();
                if (status.equalsIgnoreCase("realisiert")) {
                    gebaeude.setPlanned(false);
                } else {
                    gebaeude.setPlanned(true);
                }
                if (art.equalsIgnoreCase("Gebaeude")) {
                    gebaeude.setUndergroundStructure(false);
                } else {
                    gebaeude.setUndergroundStructure(true);
                }
                if (bb_egid != null) gebaeude.setEgid(Integer.valueOf(bb_egid));
                
                Geometry intersection = null;
                intersection = parcelGeom.intersection(gebaeudeGeometry);
                logger.debug(intersection.toString());
                logger.debug("intersection.getArea() {}", intersection.getArea());
                
                double intersectionArea = intersection.getArea();
                double gebaeudeArea = gebaeudeGeometry.getArea();
                logger.debug("intersectionArea {}", intersectionArea);
                logger.debug("gebaeudeArea {}", gebaeudeArea);
                
                // Ignore building if it is less than minIntersection on the parcel.
                if (intersection.isEmpty() || intersectionArea < minIntersection) {
                    return null;
                }
                
                // Falls der Unterschied zwischen dem Gebäude-Grundstück-Verschnitt und 
                // dem gesamten Gebäude kleiner als minIntersection ist, ist das Gebäude
                // vollständig auf dem Grundstück.
                if (Math.abs(intersectionArea - gebaeudeArea) < minIntersection) {
                    gebaeude.setArea(gebaeudeArea);
                } else {
                    gebaeude.setAreaShare(intersectionArea);
                }
 
                byte gebaeudeWkbGeometry[] = geomEncoder.write(gebaeudeGeometry);

                String stmt = "SELECT ge.t_id, lokname.atext AS strassenname, ge.hausnummer, plz.plz, ortname.atext AS ortschaft, ge.astatus, ge.lage, ge.gwr_egid AS geb_egid, ge.gwr_edid \n" +
                        "FROM \n" +
                        "    "+getSchema()+"."+TABLE_DM01VCH24LV95DGEBAEUDEADRESSEN_GEBAEUDEEINGANG+" AS ge \n" + 
                        "    LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DGEBAEUDEADRESSEN_LOKALISATIONSNAME+" AS lokname \n" + 
                        "    ON ge.gebaeudeeingang_von = lokname.benannte \n" + 
                        "    LEFT JOIN "+getSchema()+"."+TABLE_PLZOCH1LV95DPLZORTSCHAFT_ORTSCHAFT+" AS ort \n" + 
                        "    ON ST_Intersects(ge.lage, ort.flaeche) \n" + 
                        "    LEFT JOIN "+getSchema()+"."+TABLE_PLZOCH1LV95DPLZORTSCHAFT_ORTSCHAFTSNAME+" AS ortname \n" + 
                        "    ON ortname.ortschaftsname_von = ort.t_id \n" +
                        "    LEFT JOIN "+getSchema()+"."+TABLE_PLZOCH1LV95DPLZORTSCHAFT_PLZ6+" AS plz \n" + 
                        "    ON ST_Intersects(ge.lage, plz.flaeche ) \n" + 
                        "WHERE ge.istoffiziellebezeichnung = 'ja' AND ge.astatus = :status AND ge.im_gebaeude = :im_gebaeude AND ST_Intersects(ge.lage, ST_GeomFromWKB(:gebaeudeGeom,2056))";
                
                MapSqlParameterSource parameters = new MapSqlParameterSource();
                parameters.addValue("gebaeudeGeom", gebaeudeWkbGeometry);
                
                if (status.equalsIgnoreCase("realisiert")) {
                    parameters.addValue("status", "real");
                } else {
                    parameters.addValue("status", "projektiert");
                }
                
                if (art.equalsIgnoreCase("Gebaeude")) {
                    parameters.addValue("im_gebaeude", "BB");
                } else {
                    parameters.addValue("im_gebaeude", "EO");
                }
                
                List<BuildingEntryType> buildingEntryList = jdbcParamTemplate.query(stmt, parameters, new RowMapper<BuildingEntryType>() {
                    @Override
                    public BuildingEntryType mapRow(ResultSet rs, int rowNum) throws SQLException {
                        String strassenname = rs.getString("strassenname");
                        String hausnummer = rs.getString("hausnummer");
                        String plz = rs.getString("plz");
                        String ortschaft = rs.getString("ortschaft");
                        String geb_egid = rs.getString("geb_egid");
                        String gwr_edid = rs.getString("gwr_edid");
                        
                        // TODO: Soll geprüft werden, ob der Eingang auf dem Grundstück liegt?
                        // Kann entweder hier gemacht werden oder bereits in der Query.
                        BuildingEntryType gebaeudeeingang = new BuildingEntryType();
                        AddressType postalAddress = new AddressType();
                        postalAddress.setStreet(strassenname);
                        postalAddress.setNumber(hausnummer);
                        postalAddress.setPostalCode(Integer.valueOf(plz));
                        postalAddress.setCity(ortschaft);
                        gebaeudeeingang.setPostalAddress(postalAddress);
                        if (geb_egid != null) gebaeudeeingang.setEgid(Integer.valueOf(geb_egid));
                        if (gwr_edid != null) gebaeudeeingang.setEdid(Integer.valueOf(gwr_edid));
                        
                        return gebaeudeeingang;
                    }
                });
                gebaeude.getBuildingEntries().addAll(buildingEntryList);
                return gebaeude;
            }            
        }, parcelWkbGeometry, parcelWkbGeometry, parcelWkbGeometry);
        realEstate.getBuildings().addAll(gebaeudeList);
    }

    private void setBodenbedeckung(RealEstateDPR realEstate, Geometry geometry) {
        WKBWriter geomEncoder = new WKBWriter(2, ByteOrderValues.BIG_ENDIAN);
        byte wkbGeometry[] = geomEncoder.write(geometry);
        
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("geom", wkbGeometry);

        List<LandCoverShareType> bbList = jdbcParamTemplate.query("SELECT ST_Area(ST_Union(geom)) AS flaechenmass, art \n" + 
                "FROM (SELECT (ST_Dump(ST_CollectionExtract(ST_Intersection(ST_GeomFromWKB(:geom,2056), b.geometrie), 3))).geom AS geom, b.art FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DBODENBEDECKUNG_BOFLAECHE+" AS b WHERE ST_Intersects(ST_GeomFromWKB(:geom,2056), b.geometrie)) AS foo \n" + 
                "WHERE ST_IsValid(geom) IS TRUE AND geom IS NOT NULL GROUP BY art", parameters, new RowMapper<LandCoverShareType>() {

            @Override
            public LandCoverShareType mapRow(ResultSet rs, int rowNum) throws SQLException {
                double flaechenmass = rs.getDouble("flaechenmass");
                String art = rs.getString("art");
                
                LandCoverShareType bb = new LandCoverShareType(); 
                bb.setType(LCType.fromValue(art));
                bb.setTypeDescription(LCType.fromValue(art).value());
                bb.setArea(flaechenmass);
                
                return bb;
            } 
        });
        realEstate.getLandCoverShares().addAll(bbList);
    }

	
	private void setFlurnamen(RealEstateDPR realEstate, Geometry geometry) {
        WKBWriter geomEncoder = new WKBWriter(2, ByteOrderValues.BIG_ENDIAN);
        byte wkbGeometry[] = geomEncoder.write(geometry);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("geom", wkbGeometry);
        
        List<LocalNameType> localNameList = jdbcParamTemplate.query("SELECT aname as flurname \n" + 
                "FROM (SELECT (ST_Dump(ST_CollectionExtract(ST_Intersection(ST_GeomFromWKB(:geom,2056), f.geometrie), 3))).geom AS geom, f.aname FROM "+getSchema()+"."+TABLE_DM01VCH24LV95NOMENKLATUR_FLURNAME+" AS f WHERE ST_Intersects(ST_GeomFromWKB(:geom,2056), f.geometrie)) AS foo \n" + 
                "WHERE ST_IsValid(geom) IS TRUE AND geom IS NOT NULL GROUP BY aname", parameters, new RowMapper<LocalNameType>() {

            @Override
            public LocalNameType mapRow(ResultSet rs, int rowNum) throws SQLException {
                String name = rs.getString("flurname");
                
                LocalNameType localName = new LocalNameType(); 
                localName.setName(name);
                return localName;
            } 
        });
        realEstate.getLocalNames().addAll(localNameList);
	}
    
	private Grundstueck getParcelByEgrid(String egrid) {
        // CH955832730623 = Liegenschaft
        // CH707406053288 = SelbstRecht.Baurecht
        // CH527732831247 = SelbstRecht.Quellenrecht
        // CH367883126943 = SelbstRecht.Konzessionsrecht
        // CH327840831216 = SelbstRecht.weitere
        // CH487706867746 = Bergwerk
        // CH310663327779 = mehrere Flurnamen
        // CH493273420604 = Grenchen GB-Nr. 4000
        // CH907006873276 = Roamer-Gebäude: 1 BB mit zwei Eingängen
        // CH670679613281 = Überbauung beim Bahnhof: viele Eingänge und Hausnummern. Angrenzendes Gebäude.
        // CH729921320631 = Neue (projektiert) Überbauung "Hufeisen" in Biberist beim Spital.

        // Mehr oder weniger copy/paste from oereb-web-service.
        // Ist ziemlich smart gemacht, da es z.B. auch "Multipolygon"-Liegenschaften
        // berücksichtigt, die es bei uns (SO) nicht gibt.
        // Und hier die Geometrie zu holen und mit dieser weiterzuarbeiten, ist 
        // auch eine gute Lösung. Sonst müsste man immer wieder eine Subquery o.ä.
        // machen.
		
        PrecisionModel precisionModel = new PrecisionModel(1000.0);
        GeometryFactory geomFactory = new GeometryFactory(precisionModel);

        List<Grundstueck> gslist = jdbcTemplate.query(
                "SELECT ST_AsBinary(l.geometrie) as l_geometrie,ST_AsBinary(s.geometrie) as s_geometrie,ST_AsBinary(b.geometrie) as b_geometrie,nummer,nbident,art,gesamteflaechenmass,l.flaechenmass as l_flaechenmass,s.flaechenmass as s_flaechenmass,b.flaechenmass as b_flaechenmass FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_GRUNDSTUECK+" g"
                        +" LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LIEGENSCHAFT+" l ON g.t_id=l.liegenschaft_von "
                        +" LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_SELBSTRECHT+" s ON g.t_id=s.selbstrecht_von"
                        +" LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_BERGWERK+" b ON g.t_id=b.bergwerk_von"
                        +" WHERE g.egris_egrid=?", new RowMapper<Grundstueck>() {
                    WKBReader decoder=new WKBReader(geomFactory);
                    
                    @Override
                    public Grundstueck mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Geometry polygon=null;
                        byte l_geometrie[]=rs.getBytes("l_geometrie");
                        byte s_geometrie[]=rs.getBytes("s_geometrie");
                        byte b_geometrie[]=rs.getBytes("b_geometrie");
                        try {
                            if(l_geometrie!=null) {
                                polygon=decoder.read(l_geometrie);
                            }else if(s_geometrie!=null) {
                                polygon=decoder.read(s_geometrie);
                            }else if(b_geometrie!=null) {
                                polygon=decoder.read(b_geometrie);
                            }else {
                                throw new IllegalStateException("no geometrie");
                            }
                            if(polygon==null || polygon.isEmpty()) {
                                return null;
                            }
                        } catch (org.locationtech.jts.io.ParseException e) {
                            throw new IllegalStateException(e);
                        }
                        Grundstueck ret=new Grundstueck();
                        ret.setGeometrie(polygon);
                        ret.setEgrid(egrid);
                        ret.setNbident(rs.getString("nbident"));
                        ret.setNummer(rs.getString("nummer"));
                        ret.setArt(rs.getString("art"));
                        int f = rs.getInt("gesamteflaechenmass");
                        if(rs.wasNull()) {
                            if (l_geometrie!=null) {
                                f=rs.getInt("l_flaechenmass");
                            } else if(s_geometrie!=null) {
                                f=rs.getInt("s_flaechenmass");
                            } else if(b_geometrie!=null) {
                                f=rs.getInt("b_flaechenmass");
                            } else {
                                throw new IllegalStateException("no geometrie");
                            }
                        }
                        ret.setFlaechenmass(f);
                        ret.setKanton(ret.getNbident().substring(0,2).toUpperCase());
                        return ret;
                    }
                }, egrid);

        if(gslist==null || gslist.isEmpty()) {
            return null;
        }
        Polygon polygons[] = new Polygon[gslist.size()];
        int i=0;
        for (Grundstueck gs : gslist) {
            polygons[i++] = (Polygon)gs.getGeometrie();
        }
        Geometry multiPolygon=geomFactory.createMultiPolygon(polygons);
        Grundstueck gs = gslist.get(0);
        gs.setGeometrie(multiPolygon);

        // Grundbuchkreis 
        try {
            Map<String,Object> gbKreis = jdbcTemplate.queryForMap(
                    "SELECT gb.aname,gb.grundbuchkreis_bfsnr,gb.bfsnr,gem.aname AS gemeindename FROM "+getSchema()+"."+TABLE_SO_G_V_0180822GRUNDBUCHKREISE_GRUNDBUCHKREIS+" AS gb" +
                    " LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DGEMEINDEGRENZEN_GEMEINDE+" AS gem ON gem.bfsnr = gb.bfsnr" +
                    " WHERE nbident=?", gs.getNbident());
            gs.setGbSubKreis((String) gbKreis.get("aname"));
            gs.setGbSubKreisNummer((int) gbKreis.get("grundbuchkreis_bfsnr"));
            gs.setBfsnr((Integer) gbKreis.get("bfsnr"));
            gs.setGemeinde((String) gbKreis.get("gemeindename"));
        } catch(EmptyResultDataAccessException ex) {
            logger.warn("no gbkreis for nbident {}",gs.getNbident());
        }
        return gs;
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
