package info.ejava.examples.svc.authn.authcfg.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest(properties = "test=true")
@AutoConfigureMockMvc
public class AuthConfigMockMvcNTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc anonymousUserMock;

    // manual instantiation
    private MockMvc authnUserMock;

    @BeforeEach
    public void init(){
        authnUserMock = MockMvcBuilders.webAppContextSetup(context)
                        .apply(SecurityMockMvcConfigurers.springSecurity())
                        .build();
    }

    @Nested
    public class when_calling_permit_all {

        private final String uri = "/api/anonymous/hello";

        @Test
        void anonymous_can_call_get() throws Exception{
            anonymousUserMock.perform(MockMvcRequestBuilders.get(uri).queryParam("name", "jim"))
                            .andDo(MockMvcResultHandlers.print())
                            .andExpect(MockMvcResultMatchers.status().isOk())
                            .andExpect(MockMvcResultMatchers.content().string("hello jim :caller = (null)"));

        }

        @Test
        void anonymous_can_call_post() throws Exception {
            anonymousUserMock.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.TEXT_PLAIN).content("jim"))
                                .andDo(MockMvcResultHandlers.print())
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.content().string("hello jim :caller =(null)"));
                            
        }

        @WithMockUser("user")
        @Test
        void user_can_call_get () throws Exception{
            authnUserMock.perform(MockMvcRequestBuilders.get(uri).queryParam("name", "jim"))
                            .andDo(MockMvcResultHandlers.print())
                            .andExpect(MockMvcResultMatchers.status().isOk())
                            .andExpect(MockMvcResultMatchers.content().string("hello jim :caller = user"));
                            
        }

        @WithMockUser("user")
        @Test
        public void user_can_call_post() throws Exception {
            authnUserMock.perform(MockMvcRequestBuilders.post(uri)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("jim"))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string("hello jim :caller =user"));
        }


    }

    @Nested
    public class when_calling_authn {
        private final String uri = "/api/authn/hello";
        @Test
        public void anonymous_cannot_call_get() throws Exception {
            anonymousUserMock.perform(MockMvcRequestBuilders.get(uri).queryParam("name","jim"))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        }
        @Test
        public void anonymous_cannot_call_post() throws Exception {
            anonymousUserMock.perform(MockMvcRequestBuilders.post(uri)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("jim"))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        }

        @WithMockUser("user")
        @Test
        public void user_can_call_get() throws Exception {
            authnUserMock.perform(MockMvcRequestBuilders.get(uri)
                    .queryParam("name","jim"))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string("hello jim :caller = user"));
        }

        @WithMockUser("user")
        @Test
        public void user_can_call_post() throws Exception {
            authnUserMock.perform(MockMvcRequestBuilders.post(uri)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("jim"))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string("hello jim :caller = user"));
        }
    }

     @Nested
    public class when_calling_alt {
        private final String uri = "/api/alt/hello";
        //WebSecurityConfigurerAdapter default denies anonymous
        //        @Test
        //        public void anonymous_cannot_call_get() throws Exception {
        //            anonymous.perform(MockMvcRequestBuilders.get(uri).queryParam("name","jim"))
        //                    .andDo(print())
        //                    .andExpect(status().isUnauthorized());
        //        }

        //Component-based default allows anonymous -- at least with our trivial configuration
        @Test
        public void anonymous_cannot_call_get() throws Exception {
            anonymousUserMock.perform(MockMvcRequestBuilders.get(uri).queryParam("name","jim"))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }
         @Test
        public void anonymous_cannot_call_post() throws Exception {
            anonymousUserMock.perform(MockMvcRequestBuilders.post(uri)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("jim"))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isForbidden());
        }

        @WithMockUser("user")
        @Test
        public void user_can_call_get() throws Exception {
            authnUserMock.perform(MockMvcRequestBuilders.get(uri)
                    .queryParam("name","jim"))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string("hello jim :caller = user"));
        }

        @WithMockUser("user")
        @Test
        public void user_cannot_call_post() throws Exception {
            authnUserMock.perform(MockMvcRequestBuilders.post(uri)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("jim"))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isForbidden());
        }

    }
    
}
