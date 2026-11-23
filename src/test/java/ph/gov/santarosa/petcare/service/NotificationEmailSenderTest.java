package ph.gov.santarosa.petcare.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ph.gov.santarosa.petcare.domain.AppUser;
import ph.gov.santarosa.petcare.repository.AppUserRepository;
import ph.gov.santarosa.petcare.support.FakeRepo;

class NotificationEmailSenderTest {
    @Test
    void brevoApiDeliversStatusNotificationsWithoutSmtp(){
        AppUserRepository users=FakeRepo.create(AppUserRepository.class,AppUser.class);
        AppUser user=new AppUser();user.setId("U-1");user.setFirstName("Ana");user.setFullName("Ana Reyes");
        user.setEmail("ana@example.com");users.save(user);
        var mailProvider=new StaticListableBeanFactory().getBeanProvider(JavaMailSender.class);
        RestClient.Builder builder=RestClient.builder();
        MockRestServiceServer server=MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email")).andExpect(header("api-key","test-key"))
                .andRespond(withSuccess("{}",MediaType.APPLICATION_JSON));
        NotificationEmailSender sender=new NotificationEmailSender(mailProvider,users,builder,true,"brevo-api",
                "test-key","petcare@example.com","PetCARE Office");

        assertTrue(sender.isEnabled());
        assertTrue(sender.send("U-1","Registration updated","Your registration was verified.").delivered());
        server.verify();
    }
}
