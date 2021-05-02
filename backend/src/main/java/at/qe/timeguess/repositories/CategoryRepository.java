package at.qe.timeguess.repositories;

import at.qe.timeguess.model.Category;

import java.util.List;

public interface CategoryRepository extends AbstractRepository<Category, Long> {

    List<Category> findByOrderByIdAsc();

	Category findFirstByName(String name);

}
