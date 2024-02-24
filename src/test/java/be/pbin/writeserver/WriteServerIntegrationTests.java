//package be.pbin.writeserver;
//
//import be.pbin.writeserver.data.metadata.NoteMetaData;
//import be.pbin.writeserver.data.metadata.NoteMetadataRepository;
//import org.apache.commons.lang3.RandomStringUtils;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
////@Testcontainers
//public class WriteServerIntegrationTests {
//
//    /** Notes on using test container:
//     *  non-static: the container will restart for every test method within this class
//     *  static: one container for the whole class, it will be shutdown after all the tests have run
//     *  -----
//     *  note: testcontainer will give container random port on every run
//     */
////    @Container
////    private static MySQLContainer mySQLContainer =
////            new MySQLContainer<>(DockerImageName.parse("mysql:8.0.36"))
////                    .withDatabaseName("myDatabase")
////                    .withUsername("root")
////                    .withPassword("password");
////
////
////    /**
////     * Overrides the property from the application properties
////     * note the method reference, so the provided method can get called later on when the container has booted
////     */
////    @DynamicPropertySource
////    public static void overrideProps(DynamicPropertyRegistry registry) {
////        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
////        registry.add("spring.sql.init.mode", () -> DatabaseInitializationMode.ALWAYS);
////    }
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Autowired
//    private NoteMetadataRepository sqlRepository;
//
////    @Test
////    void testContainerShouldInitSchemaAndLoadDummyData() {
////        assertTrue(sqlRepository.existsById("XyZ987K5")); //cf. data.sql
////        assertTrue(sqlRepository.existsById("PqR67Z90"));
////    }
//
//    @Test
//    void testContainerShouldBeWritableAndReadable() {
//        String shortlink = "shrtlink";
//        String path = RandomStringUtils.randomAlphabetic(10);
//        NoteMetaData note = NoteMetaData.builder()
//                .shortLink(shortlink)
//                .creationDate(LocalDateTime.now())
//                .expirationTime(10)
//                .path(path).build();
//        sqlRepository.save(note);
//
//        assertTrue(sqlRepository.existsById(shortlink));
//
//        Optional<NoteMetaData> optionalnote = sqlRepository.findById(shortlink);
//        assertThat(optionalnote).isPresent();
//        assertThat(optionalnote.get().getShortLink()).isEqualTo(shortlink);
//        assertThat(optionalnote.get().getPath()).isEqualTo(path);
//        assertThat(optionalnote.get().getExpirationTime()).isEqualTo(10);
//    }
//
//
//}
