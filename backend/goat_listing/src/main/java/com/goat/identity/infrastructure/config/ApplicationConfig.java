package com.goat.identity.infrastructure.config;

import com.goat.identity.adapters.persistence.PostgreSQLRoleRepository;
import com.goat.identity.adapters.persistence.PostgreSQLUserRepository;
import com.goat.identity.application.usecases.ConfirmEmailUseCase;
import com.goat.identity.application.usecases.CreateUserUseCase;
import com.goat.identity.application.usecases.GenerateOtpUseCase;
import com.goat.identity.application.usecases.LoginUserUseCase;
import com.goat.identity.application.usecases.VerifyOtpUseCase;
import com.goat.identity.ports.OtpGenerationPort;
import com.goat.identity.ports.OtpValidationPort;
import com.goat.identity.ports.OtpVerificationPort;
import com.goat.identity.ports.PasswordEncoderPort;
import com.goat.identity.ports.RoleRepository;
import com.goat.identity.ports.TokenGeneratorPort;
import com.goat.identity.ports.UserRepository;
import com.goat.listing.adapters.persistence.PostgreSQLListingRepository;
import com.goat.listing.application.usecases.ArchiveListingUseCase;
import com.goat.listing.application.usecases.CreateListingUseCase;
import com.goat.listing.application.usecases.CreateSneakerUseCase;
import com.goat.listing.application.usecases.DeleteListingUseCase;
import com.goat.order.adapters.persistence.repository.OrderItemJpaRepository;
import com.goat.listing.application.usecases.GetListingUseCase;
import com.goat.listing.application.usecases.GetListingsUseCase;
import com.goat.listing.application.usecases.GetMyListingsUseCase;
import com.goat.listing.application.usecases.GetSneakerUseCase;
import com.goat.listing.application.usecases.PublishListingUseCase;
import com.goat.listing.application.usecases.SearchSneakersUseCase;
import com.goat.listing.application.usecases.UpdateListingUseCase;
import com.goat.listing.ports.CatalogServicePort;
import com.goat.listing.ports.ListingRepository;
import com.goat.navigation.adapters.persistence.PostgreSQLMenuRepository;
import com.goat.navigation.application.usecases.GetMenusUseCase;
import com.goat.navigation.ports.MenuRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la aplicación.
 * Define los beans necesarios para la arquitectura hexagonal.
 */
@Configuration
public class ApplicationConfig {
    @Bean
    public LoginUserUseCase loginUserUseCase(
            UserRepository userRepository,
            PasswordEncoderPort passwordEncoder,
            TokenGeneratorPort tokenGenerator) {
        return new LoginUserUseCase(userRepository, passwordEncoder, tokenGenerator);
    }

    @Bean
    public CreateUserUseCase createUserUseCase(
            UserRepository userRepository,
            PasswordEncoderPort passwordEncoder,
            RoleRepository roleRepository) {
        return new CreateUserUseCase(userRepository, passwordEncoder, roleRepository);
    }

    @Bean
    public ConfirmEmailUseCase confirmEmailUseCase(
            UserRepository userRepository,
            OtpVerificationPort otpVerificationPort) {
        return new ConfirmEmailUseCase(userRepository, otpVerificationPort);
    }

    @Bean
    public GenerateOtpUseCase generateOtpUseCase(OtpGenerationPort otpGenerationPort) {
        return new GenerateOtpUseCase(otpGenerationPort);
    }

    @Bean
    public VerifyOtpUseCase verifyOtpUseCase(
            OtpValidationPort otpValidationPort,
            UserRepository userRepository) {
        return new VerifyOtpUseCase(otpValidationPort, userRepository);
    }

    @Bean
    public UserRepository userRepository(PostgreSQLUserRepository postgreSQLUserRepository) {
        return postgreSQLUserRepository;
    }

    @Bean
    public RoleRepository roleRepository(PostgreSQLRoleRepository postgreSQLRoleRepository) {
        return postgreSQLRoleRepository;
    }

    @Bean
    public MenuRepository menuRepository(PostgreSQLMenuRepository postgreSQLMenuRepository) {
        return postgreSQLMenuRepository;
    }

    @Bean
    public GetMenusUseCase getMenusUseCase(MenuRepository menuRepository) {
        return new GetMenusUseCase(menuRepository);
    }

    // Listings Service beans
    @Bean
    public ListingRepository listingRepository(PostgreSQLListingRepository postgreSQLListingRepository) {
        return postgreSQLListingRepository;
    }

    @Bean
    public CreateListingUseCase createListingUseCase(
            ListingRepository listingRepository,
            CatalogServicePort catalogServicePort) {
        return new CreateListingUseCase(listingRepository, catalogServicePort);
    }

    @Bean
    public UpdateListingUseCase updateListingUseCase(ListingRepository listingRepository) {
        return new UpdateListingUseCase(listingRepository);
    }

    @Bean
    public PublishListingUseCase publishListingUseCase(ListingRepository listingRepository) {
        return new PublishListingUseCase(listingRepository);
    }

    @Bean
    public ArchiveListingUseCase archiveListingUseCase(ListingRepository listingRepository) {
        return new ArchiveListingUseCase(listingRepository);
    }

    @Bean
    public DeleteListingUseCase deleteListingUseCase(
            ListingRepository listingRepository,
            OrderItemJpaRepository orderItemJpaRepository) {
        return new DeleteListingUseCase(listingRepository, orderItemJpaRepository);
    }

    @Bean
    public GetListingUseCase getListingUseCase(ListingRepository listingRepository) {
        return new GetListingUseCase(listingRepository);
    }

    @Bean
    public GetListingsUseCase getListingsUseCase(ListingRepository listingRepository) {
        return new GetListingsUseCase(listingRepository);
    }

    @Bean
    public GetMyListingsUseCase getMyListingsUseCase(ListingRepository listingRepository) {
        return new GetMyListingsUseCase(listingRepository);
    }

    // Catalog Service beans
    @Bean
    public GetSneakerUseCase getSneakerUseCase(CatalogServicePort catalogServicePort) {
        return new GetSneakerUseCase(catalogServicePort);
    }

    @Bean
    public CreateSneakerUseCase createSneakerUseCase(CatalogServicePort catalogServicePort) {
        return new CreateSneakerUseCase(catalogServicePort);
    }

    @Bean
    public SearchSneakersUseCase searchSneakersUseCase(CatalogServicePort catalogServicePort) {
        return new SearchSneakersUseCase(catalogServicePort);
    }
}

