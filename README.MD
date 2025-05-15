

@SprinmgBootTest(classes=GreeterApplication.class, webEnvironment=SpringBootTest.WebEnvironmnt.RANDOM_PORT)
public classs GreeterRestTemplateNTest {

	@LocalServerPort
	private int port;
	
	
}


@LocalServerPort is an alias of local.server.port

@Value("${local.server.port")
public @interface  LocalServerPort {

		int value;
}


as you saw earlier we can inject port as an attribute value of the test case class . The @Test method can use this port value 

another way  is to use this port value in the lifecycle method of the test case, the @Test method may not use the raw port value but instead it may use 
something that is built from the port value

@BeforeEach
void init(@LocalServerPort int port){
	String url = String.format("http://localhost:%d/greeter/greet/sayHi",port);
}

We can move the injection to the @TestConfiguration 

@TestConfiguration(proxyBeanMethod=false)
public class GreeterTestConfiguration {
	
	@Bean @Lazy
	public String baseUrl(@LocalServerPort port){
		return String.format("http://localhost:%d/greeter/greet/sayHi",port);
	}
	
}


@SpringBootTest(classes={GreeterApplication.class,GreeterTestConfiguration.class},webEnvironment=SpringBootTest.webEnvironment.RANDOM_PORT)
 public class GreeterRestTemplateNTest {
	
	@Autowire @Qualifier("baseUrl")  // Qualifier makes bean selection more explicit
	private String injectBaseUrl;  // initialize in test config using way3
	 
 }
 
 with a url in hand , we are ready to make a call. we are using synchronous RestTemplate from Spring MVC library
 
 RestTemplate restTemplate = new RestTemplate();
 
 String response = restTemplate.getForObject(url,String.class);
 
 synchronous restTemplate make calls to the end point url using HTTP GET method and returning the response body content as string

RestClientException -> RestClientResponseException -> HttpStatusCodeException -> HttpServerErrorException -> HttpClientErrorException 
BDDAssertions.then(greeting).isEqualTo("hi");

Synchronous  - RestTemplate
fluent and synchronous - RestClientException
fluent , asynchronous and reactive - WebClient
RestTemplate - RestClient - WebClient - Http Interface

RestClient is an synchronous API but fluent, fluent means it uses chaining

RestClient restClient = RestClient.builder().build();
retsClient = RestClient.create(restTemplate);

String greeting = restclient.get().uri(url).retrieve().body(String.class);

String resp = restClient.get().uri(url).retrieve().body(String.class);
BddAssertions.then(resp).isEqualTo("hi");

WebClient webClient = WebClient.builder().build();
String greeting = webClient.get().uri(url).retrieve().bodyToMono(String.class).block(); // calling block causes the reactive flow definition to begin producing data


@TestConfiguration
public class HttpInterfaceConfig{

	@Bean @Lazy
	public String baseUrl(@LocalServerPort int port){
		return String.format("http://localhost:%d/greeting/");
	}
}

public interface MyGreeter {
	@GetExchannge("/sayHi");
	String sayHi();
	
}



@SpringBootTest(classes= {GreeterApplication.class, HttpInterfaceConfig.class},webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HttpInterfaceNTest {
	@Autowire @Qualifier("baseUrl")
	String baseUrl;
	
	@Test
	void test(){
		// given / Arrange
		RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();
		RestClientAdapter adapter = RestClientAdapter.create(restClient);
		HttpServiceProxyFactory  factory = HttpServiceProxyFactory.buildFor(adapter).build();
		MyGreeter greeterApi = factory.createClient(MyGreeter.class);
		
		// when calling the service  / act
		String resp = greeterApi.sayHi();
		
		// then / assert
		BddAssertions.then(resp).isEqualTo("hi");
	}
	
	
	The Spring HTTP Interface is RPC oriented but we can make it REST-Like enough to be useful

}


Implementing Parameters

-> @RequestBody - request body see in POST
-> @PathVariable pathparam with place holder {pathgParam} -> @RequstParam is the query string

you can have 0 to N Path and query param

@RequestMapping(path="say/{greeting}",method=RequestMethod.GET)
public String sayGreeting(@PathVariable("greeting") String greeting,@RequestParam(value="name", defaultValue="you")String name){
	return greeting +" , " + name; 
}

@TestConfiguration(proxyBeanFactoryMethod=false)
public class TestConfiguration {

	@Bean @Lazy
	public String baseUrl(@LocalServerPort int port){
		return String.format("http://localhost:%d/greeter", port);
	}
}

@Test
void say_greeting(){
	URI url = UriComponentsBuilder.fromHttpUrl(baseUrl).path("/say/{greeting}").queryParam("name","{name}").build("hello","jim");
}


a few prominent response making can be determined automatically by the container in simplistic cases
the response body is automatically set to marshalled value and it is a literal string mpping
the Response status / code is returned by the container is as follows - 200 / OK  - when the method returns non-null value
404 / NOT_FOUND - when the method returns a null value
500 / INTERNAL_SERVER_ERROR - when the method throws exception

container sensibly returns response headers text/plain to Content-Type

> HTTP/1.1 200
> Content-Type:text/plain;charset-utf8
Content-Length:2
....
hi

Synchronous RestTemplate
Synchronous and fluent RestClient
Asynchronous / Reactive WebClient
client side Api facade - Http interface

RestClient restCient = RestClient.builder().build();
RestClient restClient = RestClient.create(restTemplate);

RstClient call method chaining and each chain return type with a constrained set of mhods

String greeting = restClient.get()
					.uri(url).retrieve().body(String.class);
					
WebClient webClient = WebClient.builder().build();
String greeting = webClient.get().uri(url).retrieve().bodyToMono(String.class).block();


form endpoint url, with the url in hand we are ready to make the call, using the default synchronous RestTemplate from Spring mvc libray
RestClientException - error occured communicating with the server
RestClientResponseException - error response received from server
HttpStatusCodeException - Http response received and Http status known
HttpServerErrorException - Http Server (5xx) errors
HttpClientErrorException - Http Client (4xx) errors (BadRequest, Not-Found, Un-Processable entity)
  
interface MyGreeter {
	@GetExchange("/sayHai")
	String sayHai();
}

we then create a RestTemplate, RestClient or WebClient object using the base url and the baseUrl + GetExchange-url  generates the end point url
String baseUrl = ...;
RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();

RestClientAdapter adapter = RTestClientAdapter.create(restClient);
HttpServiceProxyFactory factory = HttpServiceProxuFactory.builderFor(adapter).build();
MyGreeter greeterAPI = factory.createClient(MyGreeter.class);

String resp = greeterAPI.sayHi();

There are three primary ways to make an http call to method input parameters
@RequestMapping(path="/say/{greeting}",method=RequestMethod.GET)
public String greeting(@PathParam("greeting") String greeting, @RequestParam(value="name", defaultValue="you)String name) {
	return String.format("{} , {}", greeting, name);
}

values for {greeting} and {name} placeholders are filled during call to build

The client library offers additional call to obtain the ResponseEntity<T>
Example RestTemplate ResponseEntity<T> call
// when asking for that greeting 
ResponseEntity<String> response = restTemplate.getForEntity(url,String.class);
ResponseEntity<String> resp = restClient.get().uri(url).retrieve().toEntity(String.class);
ResponseEntity<String> resp = webClient.get().uri(url).retrieve().toEntity(String.class).block();

RestClient has 2 primary path to invoke a request retrieve(), exchange()

React is a javascript Library/Framework for building user interfaces
UIs are looked at in terms of components
React is currently most popular than 

React let you build user iuinterfaces out of individual pieces called components React let you build ui out of individual pieces called components

function Video({video}){
	return (
	
		<div>
			<Thumpnail video={video} />
			<a href={video.url} >
				<h3> {video.title}   </h3>
				<p>{video.description}</p>
			</a>
			<LinkButton video={video} />
		</div>
	
	);
}


React is actually a UI library , SPA aingle page app. SSR server Side Rendering and static website
creating components , passing props and managing state
reusable piece of code that can be used to build elements on the page 
reusable piece of code that can be use to build elemnts on the page , allow us to break down complex ui which makes them easier to maintain and scale
allow us to break down complex ui, which makes them easier to maintain and scale
components can get props passed in and can hold its own state componet can get props passed in 
components can get props passed in and hold their own state

staets are data that component manages internally , states are form user input data, fetch data from API, UI related data like a modal is opened or close
two type of state - Component state and global state

An HTML like syntax with in java script, javascript syntax extension

const Hero = ({title='Become a React Dev', subtitle='find the react job '}) => {
return (
	<section className='bg-indigo-700 py-20 mb-4'>
		<div className='maxw7xl'>
			<div className=''>
				<h1 className=''>{title}
				</h1>
				<p className=''>{subtitle} </p>
			</div>
		</div>
	</section>
	
);

export default Hero;

responds with en explicit error if there is a problem with the input
responds with a specific status code and contentlocation header if successful
the service provides control over the entire response by returning a ResponseEntity contains the Status Code , Http Headers and the message body
the response entity can express the status code, http headers and the result value for the body. the response entity can express the headers, status
cod and returned entity

@RequestMapping(path="/boys", method=RequestMethod.GET)
public ResponseEntity<String> createBoy(@RequestParam("name") String name) {
	
	try {
		someMethodThrowsException(name);
		
		String location= ServletUriComponentsBuilder.fromCurrentRequest()..build().toUriString();
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_LOCATION,url).body(String.format("Hello %s, how do you do ?" , name));
		
	} catch(Exception ex){
		return ResponseEntity.unprocessableEntity().body(ex.toString());
	}
	private void someMethodThrowsException(String name){
		if("blue".equalsIgnoreCase(name){
			throw new IllegalArgumentException("boy named blue?");
		}
	}
}

ResponseEntity useees to describe http response
ServletUriComponentsBuilder provide the context of current call
service able to return explicit HTTP response with appropriate success detail
service able to return explicit Http response with appropriate error details

curl -v http://localhost:8080/rpc/greeter/boys?name=joy

...
>> HTTP/1.1 200 OK
>> Content-Location: http://localhost:8080/rpc/greeter/boys?name=jim
>> Content-Type: text/plain;charset=UTF-8
>> Content-Length:25
>> Date: 
...
hello jim, how do you do?

};

400 - Bad Request, 401- Unauthorized, 404-Not Found, 422- Unprocessable entity   client error
500 - server error
300- redirect msg

200 - success code
for the error we get the error response with specific payload

curl -v http://localhost:8080/rpc/greeter/boys?name=blue
...
>> HTTP/1.1 422 
>> Content-Type: text/plain;charset=UTF-8
>> Content-Length: 15
...
boy named blue?

Controller exception handler

if any exception occurs in controller method the the exception can be handled by te controller's exception handle method annotated with @EsceptionHandler
ther e are two types of handler for handlinbg exception - 
1. Controller Exception handler method2,. 
2. Global exception handler method by all controller

the handler method is called when exce-ption occured and it can generate the Error response details wit status codde, headers and error as the body

@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<String> handleException(IllegalArgumentException ex){
	return ResponseEntity.unprocessableEntity().body(ex.getMessage());
}

Handle all IllegalArgumentException.class thrown by the controller method
input param is concrete type or parent type
handler build the response entity with the details f the error

identified 2 primary paradigm (synchronous and reactive), 2 web frameworks (Spring MVC and Webflux) for implementing web processing and comm
2 paradigm of web processing and communications are - synchronous and reactive
Synchronous paradigm implemented by Spring ?MVC
Reactive paradigm implemented by Spring Webflux

implement a http endpoint for URI  and and http method  using spring MVC annotated in a fully synchronous mode 
implement http end point for URI and method using Sring MVC annotated controller in a fully synchronous mode
demonstrate how parameters are passing from client to service using path and query parameters
@RequestBody to identify the pay load in side request body in POST method
@PathParam which is a required param
@RequestParam which may be optional or required   demonstrate how to pass parameter between client and service using Path and request parameter

demonstrate how to pass parameters between client and services using path and request parameter
demostrate how to pass parameters between client and services using path and request parameter
demonstrate how to pass responses from the service to the client using http status code, response header and response body
Demonstarte how to explictly set http response in the service
demonstrate how to clean up service logic using exceptionhandler demonstrate how to clean up service logic using exception handler

demonstarte how to clea demonstarteb use of Synchronous Spring mvc RestTemplate, RestClient and reactive Webclient client API
spring http interface to wrap low level client API with a type safe business interface spring htytp

2 types of states , component state and global state

Route , createBrowserRouter, createRoutesFromElement,RouterProvider

Route, RuterProvider, createBrowserRoute, createRoutersFromElement
Router , RouterProvider, createBrowserRouter, createRoutesFromElement


overall endpoint break down into a set of facade and business logic 

-> the controller class having the role of facade
-> encapsulate business logic into seperate service class 
-> establish some interface pattern between the two layers so that the web facade is as clean as possible 

implement a service clas for the business logic, turn the RestController into a web facade and delegate the business logic to the injected servic class
identify error reporting strategy options exception design set of condition specific exception
implement @RestControllerAdvice class to offload exception handling and error reporting from the @RestController

we have two twchniques to report error details  1. Complex Object Result and thrown exception
we have 2 techniques to report error details - complex object result and thrown exception
we have 2 techniques to report error details - complex object result and thrown exception
exceptions are declared to carry failure specific error reporting exceptions are declared to carry failure specific error reporting
exceptions are declared to carry failure specific error reporting exceptions are declared for failure specific error reporting

Service throws ecxeption of  type of error thrown exception give the caller the option to handle or delegate
thrown exception give the caller optyion to handle or delegate
thrown exception give the caller option to handle or delegate
as long as we can create error specific exceptions 

what specific error should our service report 
what type of information should be reported what specific error should our service report what type of information should be reported 
what type of error should our service report ? what type of information should be reported status , headers (location, content type, datetime
HTTP error code is the good place to start thinking of error types Http srtaus code is good place of thinking about error typesand group them 
Http statuss code is a good place of thinking about error types and group them it is used by the world largest information system WWW

2XX success 200 - OK, 201-created
3XX redirectional 
4xx Client error 401 - unauthorize, 403 - Forbidden, 404 - Not Found, 422- Unpprocessable Entity, 400 - Bad Request
5xx Server Error
2 primary types of error - Client Based and Server Based
It is convenient to group them in a single hierarchy depending on how we define the details of Exception It could be convinient 
to group them into a hierarchy  depending on how we define the 
Checked Exceptions are inherited from java.lang.Exception and are rquired to be handled by the try , catch block r rethrown by the calling method quite tedious when building layer of methods

these exceptions are inherited from java.lang.RuntimeException and need not  to be handled by the calling method
n common serviceErrorExceptionParent if we make them different i.e. ServerErrorException unchecked and ClientErrorException Checked then we will have to create seperate inheritance hierarchies i.e no common parent for ServiceErrorException


public abstratct class ClientErrorException extends RuntimeException {

 protected ClientErrorException(Throwable cause){
    super(cause);
 }
 
 protected ClientErrorException(String msg, Object... args){
	super(String.format(msg, args);
 }
 
 protected ClientErrorException(Throwable cause, String msg, Object... args) {
	super(String.format(msg, args), cause);
 }
 
 public static class NotFoundException extends ClientErrorException {
 
	public NotFoundException(String msg, Object... args){
		super(msg,args);
	}
	public NotFoundException(Thgrowable cause, String msg, Object... args){
		super(cause, msg, args);
	}
	
	
 }

	public static class InvalidInputException extends ClientErrorException {
	
		public InvalidInputException(String msg, Object.. args){
			super(msg,args);
		}
		
		public InvalidInputException(Throwable cause, String msg, Object... args) {
			super(cause, msg, args);
		}
	}
}

if(null== gesture){
	throw new ClientErrorException.NotFoundException("geture type [%s] not found", gestureType);

}


public abstract class ServerErrorException extends RuntimeException {

	protected ServerErrorException(Throwable cause) {
		super(cause);
	}
	
	protected ServerErrorException(String msg, Object... args){
		super(String.format(msg,args);
	}
	
	protected ServerErrorException(Throwable cause,String msg, Object... args){
		super(String.format(msg,args), cause);
	}
	
	public static class InternalServerErrorException extends ServerErrorException {
		public InternalServerErrorException(String msg, Object... args){
			super(msg,args);
		}
		
		public InternalServerErrorException(Throwable cause, String msg, Object... args) {
			super (cause,msg,args);
		}
	}
}

try {

} catch(RuntimeException ex) {
	throw new ServerErrorException.InternalServerErrorException(ex, "unexpected error gettig gesture [%s] ", gestureType);
}

establish some interface pattern between the two layers so that the web facade is as clean as possible

implement service class to encapsulate business logic , turn @RestController into a web facade and delegate the business logic to the injected service class
identify error reporting strategy option
requested work to the business logic and result from the business logic requested work to the business logic and result from the business logic

this provides an adapter around business logic 

we have two technique to report error details - complex object result and thrown exception
we have 2 techniques to report error details - complex object result and thrown xception
returned method type is complex enough to carry both type of payloads complex return object require handling logic in caller
complex return object require handling logic in caller
thrown exception give the caller the option to handle and delegate  thrown exception give the caller the option to handle or delegate

AS LONG AS we create error specific exceptions we can create some cleaner handling options in the controller


public abstract class ServerErrorException extends RuntimeException {
	
	protected Instant date = Instant.now();
	protected String error;
	
	public ServerErrorException withDate(Instant date){
		this.date = date;
		return this;
	}
	
	public ServerErrorException withError(String error){
		this.error = error;
	}
	
	protected ServerErrorException(Throwable cause){
		super(cause);
	}
	
	protected ServerErrorException(String msg, Object... args) {
		super(String.format(msg,args));
	}
	
	protected ServerErrorException(Throwable cause, String msg, Object... args) {
		super(String.format(msg,args),cause);
	}
	
	public static class InternalServerErrorException extends ServerErrorException {
		public InternalServerErrorException(String msg, Objetc... args){
			super(msg,args);
		}
		public InternalServerErrorException(Throwable cause, String msg, Object... args) {
			super(cause,msg,args);
		}
	}

}


public abstract class ClientErrorException extends RuntimeException {

	protected Instant date = Instant.now();
	protected String error;
	
	public ClientErrorException withDate(Instant date) {
		this.date = date;
		return this;
	}
	
	public ClientErrorException withError(String error){
		this.error = error;
		return this;
	}
	
	protected ClientErrorException(Throwable cause) {
		super(cause);
	}
	
	protected ClientErrorException(String msg , Object... args){
		super(String.format(msg,args));
	}
	
	protected ClientErrorException(Throwable cause, String msg, Object... args) {
		super(String.format(msg,args),cause);
	}
	
	public static class NotFoundException extends ClientErrorException {
	
		public NotFoundException(String msg, Object... args){
			super(msg,args);
		}
		public NotFoundException(Throwable cause, String msg, Objetct... args){
			super(cause, msg, args);
		}
	}
	
	public static class InvalidInputException extends ClientErrorException {
		
		public InvalidInputException(String msg, Object... args){
			super(msg, args);
		}
		public InvalidInputException(Throwable cause, String msg, Objetc... args) {
			super(cause, msg, args);
		}
	}
	
	public static class BadRequestException extends ClientErrorException {
		public BadRequestException(String msg, Object... args){
			super(msg,args);
		}
		public BadRequestException(Throwable cause, String msg, Object... args){
			super(cause, msg,args);
		}
	}
	
	public static class UnAuthorizeException extends ClientErrorException {
		
		public UnAuthorizeException(String msg, Object... args){
			super(msg,args);
		}
		
		public UnAuthorizeException(Throwable cause, String msg, Object... args){
		  super(cause,msg,args);
		}
	}
	
}


@RestControllerAdvice(scanBasePackages = GuestController.class)
public class ExceptionAdvice {
	
	private ResponseEntity<String> buildResponse(HttpStatus status, String text){
		return ReponseEntity.status(status).body(text);
	}
	
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<String> handle(NotFoundException ex) {
		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
	}
	
	@ExceptionHandler(InvalidInputException.class)
	public ResponseEntity<String> handle(InvalidInputException ex) {
		return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY,ex.getMessage());
	}
	
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<String> handle(BadRequestException ex) {
		return buildResponse(HttpStatus.BAD_REQUEST,ex.getMessage());
	}
	
	@ExceptionHandler(InternalServerErrorException.class)
	public ResponseEntity<String> handle(InternalServerErrorException ex){
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}
	
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<String> handle(RuntimeException ex){
		log.warn("Error - {} {}", ex.getMessage(), ex);
		String text = String.format("unexpected error - %s", ex.toString());
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, text);
	}

}


web content is shared in many specific MIME type, Multipurpose Internet Mail Extension. we discuss about 2 of them - XML and JSON and will show manual
approaches of marshalling and unmarshalling. however content is autumatically marshalled and unmarshalled by the web client container once everything is set up automatically identify common standard information exchange content type for web api communication

manually marshall and unmarshall java types to and from stream of bytes for multiple content type manually marshall and unmarshall content type to and from byte of stream
negotiate content type when communicating using web api
design a setb of daa transfer object to render information from and to service define java class content type mapping to customize marshalling and unmarshalling
specify content types consumed and produce by a controller specify content type supplied and accept by the client
there can be multiple views of the same conceptual data managed by the service
there ca be multiple views of the same conceptual data managed by the service  client view called DTO internal; view is the business objet BO 
too much information r behavior to transfer to the remote client
Busines Object data used directly by the service 

client may gt data that they do not need
client may get data that they can not handle]\
client may get data that they are not authorized
client may get too much data to be useful (entire database to be serialized to the client)

define a set of data that is appropriate for transfering request and response between client and service
define a remote qwed hahdhdhd eueueuhfueu f
remote facade constructs data transfer object from Business object that are appropriate for remote client view
repmote facade constructs data transfer objects from business object that is appropriate for client view 

remote facade use DTO to construct and locate business object to communicate with business logic
DTOs and Bos can be the same class  in the small services 

@NoArgConstructor
@AllArgConstructor
@Data
@Builder // @Builder will create a new instance of the class using incrementally defined properties
@With

public  class  QuoteDTO {

	private int id;
	private String author;
	private String text;
	private LocalDate date;
	private String ignored;
}


@Builder require @AllArgConstructor and unmarshaller require no arg constructor

java.util Date , Calender
java.time Instant, OffsetDateTime, ZonedDateTime, LocalDate, LocalTime, LocalDateTime, Duration, Period

ObjectMapper mapper = new ObjectMapper();

ObjectMapper  mapper = new Jackson2ObjectMapperBuilder()
							.featuresToEnable(SerializationFeature.INDENT_OUTPUT)
							.featuresToDisable(SerializationFeature.WRITE_DATE_AS_TIMESTAMP)
							.createXmlMapper(false) // same spring builder creates both XML and JSON ObjectMappers
							.build();
			
ObjectMapper mapper = new Jackson2JasonObjectMapperBuilder()
							.featuresToEnable(SerializationFeature.INDENT_OUTPUT)
							.featuresToDisable(SerializationFeature.WRITE_DATE_AS_TIMESTAMP)
							.createXmlMapper(false)
							.build();
							
							ISO based string 
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public Jacson2ObjectMapperBuilderCustomizer jacksonMapper() {
	  return (builder) -> builder.featuresToEnable(SerializationFeature.INDENT_OUTPUT)
									.featuresToDisable(SerializationFeature.WRITE_DATE_AS_TIMESTAMP)
									.dateFormat(new ISODateFormate());
	
	}
							
	prvate ObjectMapper mapper ;
	
	public <T> String marshallDtoToJsonString(T object) throws IOException {
		StringWriter buffer = new StringWriter();
		mappr.writeValue(buffer,object);
		return buffer.toString();
	}

	public <T> T unmarshallJsonStringToObject(Class<T> type, String buffer) throws IOExceptio {
	
	     T result = mapper.readValue(buffer,type);
		 return result;
	}

formate and parsing time related field in text field like XML and JSON

formatting and parsing time related field in text field like XML and JSON

4 types of data marshaller - 1. Jackson JSON 2. Jackson XML 3. JSON Binding Json B 4. Java Architectture for XML Binding JAXB
Jackson JSON - default JSON provider in spring and spring boot it implements its own properitory interface for for mapping Java POJO to Json text

ObjectMapper mapper =  new Jackson2ObjectMapper();

public <T> String marshall(T object) throws IOException {
	StringWriter buffer = new StringWriter();
	mapper.writeValue(buffer, object);
	return buffer.toString();
}

public <T> T unmarshall(Class<T> type, String buffer) throws IOException {

	T result = mapper.readValue(buffer,type);
	return result;
}
If there is no match between contet-type and consumes then 415/Unsupported Media Type error status will return and if there is no match between Accept and produces then 406/Not Acceptable  error status is returned. This content negotiation and marshalling.unmarshalling is hidden from the controller

Jackkson Json has built in mapping to ISO for java.util.Date and java.time

JsonbConfig config = new JsonbConfig().setProperty(JsonbConfig.FORMATTING, true);
Jsonb builder = JsonbBuilder.create(config);

public <T> String marshall(T object) {
	String buffer = builder.toJson(object);
	return buffer;
}

public <T> T unmarshall(Class<T> type, String buffer) {
	T result = (T) builder.fromJson(buffr,type);
	return result;
}

JSON-B maven aspect
<dependency>
	<groupId>jakarta.json</groupId>
	<srtifactId>jakarta.json-api</artifactId>
</dependency>


If there is no match between Content-Type and consumes then 415/Unsupported Media eror status is returned, if no match between Accept and produces then 406/ Not Acceptable error status is returned. Most of this content negotiation and marshalling/unmarshalling is hidden from the controller

<Quote> is the root element / we want certain properties in DTO not to show in XML
xml namespace we want certain properties in \DTO not to show in XML
use id as an attribute we want certain properties not to show in xml

@NoArgConstructor
@AllArgConstructor
@Data
@Builder
@With
@JacksonXmlRootlement(localName="quote",namespace = "urn:ejava.svc-controllers.quotes")
public class QuoteDTO {
	@JacksonXmlProperty(isAttribute=true)
	private int id;
	private String author;
	private String text;
	private LocalDate date;
	@JsonIgnore
	private String ignored;
}

}

JAXB does not have mapping to Java.util.Date and java.time , so its need adapter to marshall and unmarshall date type from text to date and vice versa

public static class LocalDateJaxbAdapter extends XmlAdapter<String,LocalDate> {

	@Override 
	public LocalDate unmarshal(String text) {
		return text == null ?  null : LocalDate.parse(text,Date|TimeFormatter.ISO_LOCAL_DATE);
	}
	
	@override
	public String marshal(LocalDate timestamp) {
		return timestamp == null ? null : DateTimeFormatter.ISO_LOCAL_DATE.format(timestamp);
	}
}

@XmlAccessorType(XmlAccessType.FIELD)
public class QuoteDTO {

...
	@XmlJavaTypeAdapter(LocalDateJaxbAdapter.class)
	private LocalDate date;	
}

// package-info.java
@XmlSchema(namespace= "urn:ejava.svc-controllers.quotes")
@XmlJavaTypeAdapter(type=LocalDate.class, value=JaxbToimeAdapters.LocalDateJaxbAdapter.class)
package info.ejava.examples.svc.content.quotes.dto

import jakarta.xml.bind.annotation.XmlSchema
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;

public <T> String marshal(T object) throws JAXBException {
	JaxbContext jbx = JaxbContext.newInstance(object.getClass());
	Marshaller marshaller = jbx.createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
	
	StringWriter buffer = new StringWriter();
	marshaller.marshal(object,buffer);
	return buffer.toString();
}

public <T> T unmarshal(Class<T> type , String buffer) throws JAXBException {

	JAXBContext jbx = JAXBContext.newInstance(type);
	Unmarshaller unmarshaller = jbx.createUnmarshaller();
	
	ByteArrayInputStream bis = new ByteArrayInputStream(buffer.getBytes());
	T result = (T) unmarshaller.unmarshal(bis);
	return result;
}

ResquestEntity<QuoteDTO> request = RequestEntity.post(quoteUrl).contentType(contentType).accept(acceptType).body(validQuote);
ResponseEntity<QuoteDTO> resp = restTemplate.exchange(request,QuoteDTO.class);


We must wrap the POJO in a RequestEntity

RequestEntity<QuoteDTO> request = RequestEntity.post(quoteUrl).contentType(contentType).accept(acceptType).body(validQuote);
Response<QuoteDTO> response = restTemplate.exchange(request,QuoteDTO.class);

1. Create a POST request with the client header
2. Express desired Content-Type for the request
3. Express desired Accept type for the response

The following shows an informnation exchange for an application/json Content-Type and Accept header

POST http://localhost:52146/api/quotes, returned CREATED/201
sent:[Accept:"application/json" , Content-Type:"application/json", Content-Length:"146"]
{
	"id":0,
	"author":"Zarquon",
	"text":"asjdhsahfafuyfiafl",
	"date":"1920-08-17"
}

rcvd:[Location:"http://localhost:52146/api/quotes/1", Content-Type:"application/json", Transfer-Encoding:"chunked", Date:"Fri, 05 June 2020", Keep-Alive:"timeout=60", Connection:"keep-alive"]
{
	"id":1,
	"author": "zara",
	"text":"lkhfhdfglsdhjgl",
	"date":"08-15-1985"
}


the example above showed http traffic and marshalled payloads, they can be very convebnioient for debugging purpose
There are 2 primary ways to examine marshalled payloads there are 2 primary ways of examining marshalling payloads

RequestEntity<QuoteDTO> request = RequestEntity.post(quotesUrl).contentType(contentType).accept(acceptType).body(validQuote);
ResponseEntity<String.class> resp = restTemplate.exchange(request,String.class);
QuoteDTO quote = unMarshall(resp);

Simple Jackson JSON Initialization
ObjectMapper mapper = new ObjectMapper();

Jackson JSON Initialization using Builder
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
...
ObjectMapper mapper = new Jackson2ObjectMapperBuilder()

  .featuresToEnable(SerializationFeature.INDENT_OUTPUT) ①
  .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) ②
  //more later
  .createXmlMapper(false) ③
  .build();

Jackson marshall Zone based time stamp as decimal number,  and gfeneric date/time as an array of values. 
By disabling this serialization faeture , jackson produces ISO-based strings for all types of timestamp and generic date/times

Example Jackson2ObjectMapperBuilder Custom Configuration
/**
 * Execute these customizations first (Highest Precedence) and then the
 * properties second so that properties can override Java configuration.
 */
@Bean
@Order(Ordered.HIGHEST_PRECEDENCE)
public Jackson2ObjectMapperBuilderCustomizer jacksonMapper() {
  return (builder) -> { builder
  //spring.jackson.serialization.indent-output=true
  .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
  //spring.jackson.serialization.write-dates-as-timestamps=false
  .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  //spring.jackson.dateformat=info.ejava.examples.svc.content.quotes.dto.ISODateFormat
  .dateFormat(new ISODateFormat());
  };
}

// Jackson JSON marshalling and unmarshalling
// Marshall DTO to JSON using jackson

public <T> String marshal(T object) throws IOException {
	StringWritter buffer = new StringWritter();
	mapper.writeValue(object,buffer);
	return buffer.toString();	
}


// umarshal JSON to DTO using Jackson

public <T> T unmarshal(Class<T> type, String buffer) throws IOException {
	T result = mapper.readValue(buffer, type);
	return result;
}

# Jackson JSON maven Aspect -  
<dependecy> 
	<groupId>com.fasterxml.jackson.core</groupId>
	<artifactId>jacksons-annotations</artifactId>
</dependency>

Jackson has built in ISO mapping for Java time and date util
Jackson map java time to decimal number, and java.Date to array value 
by disabling these features Jackson map the timer and uti date to ISO string

@JsonIgnore // jackson annotation to identify unmapped java properties @JsonIgnore
@JsonbTransient // Jsonb annotation to identify unmapped java properties


JSON-B  initialization

Jsonb provides all mappings through a Jsonb builder object that can be configured with various options

JsonbConfig  config = new JsonbConfig().setProperty(JsonbConfig.FORMATTING, true);
Jsonb builder = JsonbBuilder.create(config);

Jsonb Marshalling and Unmarshalling

Marshall DTO using JsonB 
public <T> String marshal(T object) {
	String buffer = builder.toJson(object);
	return buffer;
}

unmarshal DTO to JSON using Jsonb

public <T> T unmarshal (Class<T> type, String buffer) {
	T result = (T) builder.fromJson(buffer,type);
	return result;
}

JSONB maven aspect - 

<dependency>
	<groupId> jakarta.json </groupId>
	<artifactId> jakarta.json-api</artifactId>
</dependency>


Example of Vanilla XML document

<QuoteDTO>
	<id>0</id>
	<author>dbfkdsbfksdgf</author>
	<text>sdmgfsgjhsigigh</text>
	<date>1970</date>
	<date>05</date>
	<date>25</date>
	<ignored>ignored</ignored>
</QuoteDTO>

Example XML document with namespace , attribute and desired shaping

<QuoteDTO xmlns="uern:ejava.svc-controllers.quotes" id="0">
	<author>djfhsdkgf</author>
	<text>dhfhdsfgshg</text>
	<date>1927-12-23</date>
</QuoteDTO>


@JacksonXmlRootElement(localName="quote", namespace="urn:ejava.svc-controllers.quotes") // JACKSON XML 
@XmlRootElement(name = "quote", namespace = "urn:ejava.svc-controllers.quotes") // JAXB - Java architecture for XML Binding
public class QuoteDTO {
	@JacksonXmlProperty(isAttribute=true) 
	private int id;
	private String author;
	private String text;
	private LocalDate date;
	@JsonIgnore   // Jackson mapping to identify ignored java properties
	@Jasonbtransient  // JsonB mapping to identify ignored java properties
	private String ignore;

}


Jackson XML Initialization
XmlMapper mapper = new Jackson2ObjectMapperBuilder()
  .featuresToEnable(SerializationFeature.INDENT_OUTPUT) ①
  .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) ②
  //more later
  .createXmlMapper(true) ③
  .build();


Jackson XML Marshalling/Unmarshalling
Marshall DTO using Jackson XML
public <T> String marshal(T object) throws IOException {
  StringWriter buffer = new StringWriter();
  mapper.writeValue(buffer, object);
  return buffer.toString();
}
Unmarshal DTO using Jackson XML
public <T> T unmarshal(Class<T> type, String buffer) throws IOException {
  T result = mapper.readValue(buffer, type);
  return result;
}

. Jackson XML Maven Aspects

<dependency>
  <groupId>com.fasterxml.jackson.dataformat</groupId>
  <artifactId>jackson-dataformat-xml</artifactId>
</dependency>

Jaxb has no default definition for java.time classes and handled with custom adapter code


public static class LocalDateJaxbAdapter  extends XmlAdapter<String, LocalDate>
{
  @Override
  public LocalDate unmarshal(String text) {
	return text == null ? null : LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
  }
  @Override
  public String marshal(LocalDate timestamp) {
	return timestamp==null ? null : DateTimeFormatter.ISO_LOCAL_DATE.format(timestamp);
  }
}


Example Mapping Custom Type to Adapter for Class Property
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
...
@XmlAccessorType(XmlAccessType.FIELD) ②
public class QuoteDTO {
...
  @XmlJavaTypeAdapter(LocalDateJaxbAdapter.class) ①
  private LocalDate date;
  
  
  
  JAXB Initialization
There is no sharable, up-front initialization for JAXB. All configuration must be done on individual,
non-sharable JAXBContext objects. However, JAXB does have a package-wide annotation that the
other frameworks do not. The following example shows a package-info.java file that contains
annotations to be applied to every class in the same Java package.


JAXB Package Annotations
//package-info.java
@XmlSchema(namespace = "urn:ejava.svc-controllers.quotes")
package info.ejava.examples.svc.content.quotes.dto;
import jakarta.xml.bind.annotation.XmlSchema;


The same feature could be used to globally apply adapters package-wide.
Example Mapping Custom Type to Adapter for Package
//package-info.java
@XmlSchema(namespace = "urn:ejava.svc-controllers.quotes")
@XmlJavaTypeAdapter(type= LocalDate.class, value=JaxbTimeAdapters.
LocalDateJaxbAdapter.class)
package info.ejava.examples.svc.content.quotes.dto;
import jakarta.xml.bind.annotation.XmlSchema;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;


JAXB Marshalling/Unmarshalling

Marshall DTO using JAXB
 
public <T> String marshal(T object) throws JAXBException {
  JAXBContext jbx = JAXBContext.newInstance(object.getClass()); ①
  Marshaller marshaller = jbx.createMarshaller();
  marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); ②
  StringWriter buffer = new StringWriter();
  marshaller.marshal(object, buffer);
  return buffer.toString();
}


Unmarshal DTO using JAXB

public <T> T unmarshal(Class<T> type, String buffer) throws JAXBException {
  JAXBContext jbx = JAXBContext.newInstance(type);
  Unmarshaller unmarshaller = jbx.createUnmarshaller();
  ByteArrayInputStream bis = new ByteArrayInputStream(buffer.getBytes());
  T result = (T) unmarshaller.unmarshal(bis);
  return result;
}


JAXB Maven Aspects

Client Filters
The runtime examples above showed HTTP traffic and marshalled payloads. That can be very
convenient for debugging purposes. There are two primary ways of examining marshalled
payloads.
Switch accepted Java type to String
Both our client and controller declare they expect a QuoteDTO.class to be the response. That
causes the provider to map the String into the desired type. If the client or controller declared
they expected a String.class, they would receive the raw payload to debug or later manually
parse using direct access to the unmarshalling code.
Add a filter
Both RestTemplate and WebClient accept filters in the request and response flow. RestTemplate
is easier and more capable to use because of its synchronous behavior. We can register a filter to
get called with the full request and response in plain view — with access to the body — using
RestTemplate. WebClient, with its asynchronous design has a separate request and response
flow with no easy access to the payload.




Example RestTemplate/RestClient Logging Filter
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
...
public class RestTemplateLoggingFilter implements ClientHttpRequestInterceptor {
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,①
  ClientHttpRequestExecution execution) throws IOException {
  ClientHttpResponse response = execution.execute(request, body); ①
  HttpMethod method = request.getMethod();
  URI uri = request.getURI();
  HttpStatusCode status = response.getStatusCode();
  String requestBody = new String(body);
  String responseBody = this.readString(response.getBody());
  //... log debug
  return response;
  }
  private String readString(InputStream inputStream) { ... }
  ...
}



Note that
reading the body of a request or response is commonly a read-once restriction. The ability to read
the body multiple times will be taken care of within the @Bean factory method registering this filter.


The following code shows an example of a @Bean factory that creates RestTemplate instances
configured with the debug logging filter shown above.

Example @Bean Factory Registering RestTemplate Filter


@Bean
ClientHttpRequestFactory requestFactory() {
  return new SimpleClientHttpRequestFactory(); ③
}
@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder,
  ClientHttpRequestFactory requestFactory) { ③
  return builder.requestFactory(
  //used to read the streams twice -- so we can use the logging filter
  ()->new BufferingClientHttpRequestFactory(requestFactory)) ②
  .interceptors(new RestTemplateLoggingFilter()) ①
  .build();
}
@Bean
public RestClient restClient(RestClient.Builder builder,
  ClientHttpRequestFactory requestFactory) { ③
  return builder //requestFactory used to read stream twice
  .requestFactory(new BufferingClientHttpRequestFactory(requestFactory)) ②
  .requestInterceptor(new RestTemplateLoggingFilter()) ①
  .build();
}


Example WebClient Logging Filter
package info.ejava.examples.common.webflux;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
...
public class WebClientLoggingFilter {
  public static ExchangeFilterFunction requestFilter() {
  return ExchangeFilterFunction.ofRequestProcessor((request) -> {
  //access to
  //request.method(),
  //request.url(),
  //request.headers()
  return Mono.just(request);
  });
  }
  public static ExchangeFilterFunction responseFilter() {
  return ExchangeFilterFunction.ofResponseProcessor((response) -> {
  //access to
  //response.statusCode()
  //response.headers().asHttpHeaders())
  return Mono.just(response);
  });
  }
}



Example @Bean Factory Registering WebClient Filters
@Bean
public WebClient webClient(WebClient.Builder builder) {
  return builder
  .filter(WebClientLoggingFilter.requestFilter())
  .filter(WebClientLoggingFilter.responseFilter())
  .build();
}


JSON-B global DATE_FORMAT Option
JsonbConfig config=new JsonbConfig()
  .setProperty(JsonbConfig.DATE_FORMAT, "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]") ①
  .setProperty(JsonbConfig.FORMATTING, true);
builder = JsonbBuilder.create(config);


JSON-B Custom Serializer Option
A better JSON-B solution would be to create a serializer — independent of deserializer — that takes
care of the formatting.
Example JSON-B Default Serializer
public class DateJsonbSerializer implements JsonbSerializer<Date> {
  @Override
  public void serialize(Date date, JsonGenerator generator, SerializationContext
serializationContext) {
  generator.write(DateTimeFormatter.ISO_INSTANT.format(date.toInstant()));
  }
}


Example JSON-B Annotation Applied
@JsonbTypeSerializer(JsonbTimeSerializers.DateJsonbSerializer.class)
private Date date;


Jackson Lenient Parser

Example Custom DateFormat Class Implementing Lenient Parser
public class ISODateFormat extends DateFormat implements Cloneable {
  public static final DateTimeFormatter UNMARSHALLER = new DateTimeFormatterBuilder
()
  //...
  .toFormatter();
  public static final DateTimeFormatter MARSHALLER = DateTimeFormatter
.ISO_OFFSET_DATE_TIME;
  public static final String MARSHAL_ISO_DATE_FORMAT = "yyyy-MMdd'T'HH:mm:ss[.SSS]XXX";
  @Override
  public Date parse(String source, ParsePosition pos) {
  OffsetDateTime odt = OffsetDateTime.parse(source, UNMARSHALLER);
  pos.setIndex(source.length()-1);
  return Date.from(odt.toInstant());
  }
  @Override
  public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos)
{
  ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
  MARSHALLER.formatTo(zdt, toAppendTo);
  return toAppendTo;
  }
  @Override
  public Object clone() {
  return new ISODateFormat(); //we have no state to clone
  }
}



DateTimeFormatter Lenient Parser Definition
public static final DateTimeFormatter UNMARSHALLER = new DateTimeFormatterBuilder()
  .parseCaseInsensitive()
483
  .append(DateTimeFormatter.ISO_LOCAL_DATE)
  .appendLiteral('T')
  .append(DateTimeFormatter.ISO_LOCAL_TIME)
  .parseLenient()
  .optionalStart().appendOffset("+HH", "Z").optionalEnd()
  .optionalStart().appendOffset("+HH:mm", "Z").optionalEnd()
  .optionalStart().appendOffset("+HHmm", "Z").optionalEnd()
  .optionalStart().appendLiteral('[').parseCaseSensitive()
  .appendZoneRegionId()
  .appendLiteral(']').optionalEnd()
  .parseDefaulting(ChronoField.OFFSET_SECONDS,0)
  .parseStrict()
  .toFormatter();
  
  
  
  An instance of my ISODateFormat class is then registered with the provider to use on all interfaces.
mapper = new Jackson2ObjectMapperBuilder()
  .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
  .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  .dateFormat(new ISODateFormat()) ①
  .createXmlMapper(false)
  .build();
  
  
  In the server, we can add that same configuration option to our builder @Bean factory.
@Bean
public Jackson2ObjectMapperBuilderCustomizer jacksonMapper() {
  return (builder) -> { builder
  .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
  .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  .dateFormat(new ISODateFormat()); ①
  };
}

Idempotence is a characteristics where a repeated event produces same result each time executed
Idempotent http method - 
GET 
getQuote(@PathParam("id")int id)
getAllQuotes(@RequestParam(name="offset",defaultValue="0")int offset, @RequestParam(name="limit", defaultValue="0")int limit), 
PUT 
updateQuote(@PathParam("id)int id,@RequestBody QuoteDTO quote) , 
DELETE 
deleteQuote(@PathParam("id")int id)
deleteAllQuote(), 
OPTION,

Non Idempotent http method 
POST createQuote(@RequestBody QuoteDTO quote)

For Idempotent http method Browser page is automatically refreshed , no warning dialogue is shown
For non idempotent http method (POST ) browser page refresh shows a warning dialogue