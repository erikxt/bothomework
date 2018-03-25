package bot.repository;

import bot.entity.QaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QaRepository extends JpaRepository<QaEntity, Integer> {
}
