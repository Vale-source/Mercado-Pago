package inncome.modulo_mp.persistence.repository;

import inncome.modulo_mp.persistence.entity.CompanyToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RepositoryRestResource(exported = false)
public interface CompanyTokenRepository extends JpaRepository<CompanyToken, Long> {
    Optional<CompanyToken> findCompanyTokenById(Optional<Long> id);
}
