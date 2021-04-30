package at.qe.timeguess.repositories;

import at.qe.timeguess.model.Category;

import java.util.List;

public interface CategoryRepository extends AbstractRepository<Category, Long> {

    // TODO: make sure Categories get ordered by ID
    List<Category> findByOrderByIdAsc();

	Category findFirstByName(String name);

}
