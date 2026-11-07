package vn.binhan.repository;

import vn.binhan.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medicine m where m.id in :ids order by m.id")
    List<Medicine> findAllByIdForUpdate(@Param("ids") List<Long> ids);
}
