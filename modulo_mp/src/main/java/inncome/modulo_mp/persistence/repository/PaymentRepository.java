package inncome.modulo_mp.persistence.repository;

import inncome.modulo_mp.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(exported = false)
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
}
