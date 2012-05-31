package okapi.shared;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

@Path("/tms")
public interface RestServices
{
	@GET
	@Path("/{tmName}/uuid")
	String getUUID(
			//@CookieParam("JSESSIONID") String id,
			//@Context HttpServletRequest req,
			//@HeaderParam("HeaderName") String header
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName);
	
	@GET
	@Path("/{tmName}/description")
	String getDescription(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName);
	
	@POST
	@Path("/{tmName}/rename")
	@Consumes("text/plain")
	public void rename(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName,
			String newName);
	
	@POST
	@Path("/{tmName}/records")
	@Consumes("application/json")
	public long addRecord(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName,
			Record record);	
	
	@GET
	@Path("/{tmName}/records/{segKey}")
	@Produces("application/json")
	public HashMap<String, String> getRecord(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName,
			@PathParam("segKey") long segKey);
	
	@PUT
	@Path("/{tmName}/records/{segKey}")
	@Consumes("application/json")
	public void updateRecord(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName,
			@PathParam("segKey") long segKey,
			Record record);
	
	@DELETE
	@Path("/{tmName}/records/{segKey}")
	public void deleteRecord(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName,
			@PathParam("segKey") long segKey);
	
	
	
	
	
	@POST
	@Path("/{tmName}/locales")
	@Consumes("text/plain")
	public void addLocale(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName,
			String locale);
	
	@DELETE
	@Path("/{tmName}/locales/{locale}")
	public void deleteLocale(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName,
			@PathParam("locale") String locale); 
		
	@POST
	@Path("/{tmName}/locales/{locale}/rename")
	@Consumes("text/plain")
	public void renameLocale(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName,
			@PathParam("locale") String locale,
			String newLocale);
	
	@GET
	@Path("/{tmName}/locales")
	@Produces("application/json")
	public List<String> getLocales(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName);	
	
	
	
	
	@POST
	@Path("/{tmName}/fields")
	@Consumes("text/plain")
	public void addField(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName,
			String field);
	
	@DELETE
	@Path("/{tmName}/fields/{field}")
	public void deleteField(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName,
			@PathParam("field") String field); 
		
	@POST
	@Path("/{tmName}/fields/{field}/rename")
	@Consumes("text/plain")
	public void renameField(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName,
			@PathParam("field") String field,
			String newField);
	
	@GET
	@Path("/{tmName}/fields")
	@Produces("application/json")
	public List<String> getFields(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName);	
	
	@GET
	@Path("/{tmName}/records")
	@Produces("application/json")
	public List<HashMap<String, String>> getPage(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName,
			@QueryParam("page") long page,
			@QueryParam("size") long size,
			@QueryParam("mode") String mode);
	
	@GET
	@Path("/{tmName}/pageCount")
	public long getPageCount(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName);
	
	@GET
	@Path("/{tmName}/segmentCount")
	public long getSegmentCount(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName);	

	@GET
	@Path("/{tmName}/export")
	@Produces("text/xml")
	public File exportTmx(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName);

	@POST
	@Path("/{tmName}/import")
	@Consumes("text/xml")
	public void importTmx(
			@Context ServletContext ctx,
			@PathParam("tmName") String tmName,
			File f);
}