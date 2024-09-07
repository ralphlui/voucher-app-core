package sg.edu.nus.iss.voucher.core.workflow.configuration;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.HstsHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import sg.edu.nus.iss.voucher.core.workflow.utility.JSONReader;

@Configuration
@EnableWebSecurity
public class VoucherCoreSecurityConfig {

	private static final String[] SECURED_URLS = { "/api/**" };
	
	
	@Value("${aws.region}")
	private String awsRegion;

	@Value("${aws.accesskey}")
	private String awsAccessKey;

	@Value("${aws.secretkey}")
	private String awsSecretKey;
	
	@Value("${aws.s3.bucket}")
	private String s3Bucket;
	
	@Value("${aws.s3.image.url.prefix}")
	private String s3ImageUrlPrefix;
	
	@Value("${aws.s3.image.public}")
	private String s3ImagePublic;
	
	@Bean
	public String getAwsRegion() {
		return awsRegion;
	}

	@Bean
	public String getAwsAccessKey() {
		return awsAccessKey;
	}

	@Bean
	public String getAwsSecretKey() {
		return awsSecretKey;
	}
	
	@Bean
	public String getS3Bucket() {
		return s3Bucket;
	}
	
	@Bean
	public String getS3ImageUrlPrefix() {
		return s3ImageUrlPrefix;
	}
	
	@Bean
	public String getS3ImagePublic() {
		return s3ImagePublic;
	}


	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.cors(cors -> {
			cors.configurationSource(request -> {
				CorsConfiguration config = new CorsConfiguration();
				config.applyPermitDefaultValues();
				return config;
			});
		}).headers(headers -> headers.addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Origin", "*"))
				.addHeaderWriter(new HstsHeaderWriter(31536000, false, true)).addHeaderWriter((request, response) -> {
					response.addHeader("Cache-Control", "max-age=60, must-revalidate");
				})).csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(
						auth -> auth.requestMatchers(SECURED_URLS).permitAll().anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST", "PUT", "PATCH")
						.allowedHeaders("*");
			}
		};
	}
	
	@Bean
	public AmazonS3 s3Client() {
		AWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
		AmazonS3 amazonS3Client = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).withRegion(awsRegion).build();
		return amazonS3Client;
	}
	
	@Bean
    public JSONReader jsonReader() {
        return new JSONReader();
    }

}
