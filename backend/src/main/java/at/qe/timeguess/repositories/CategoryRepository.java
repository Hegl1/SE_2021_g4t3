package at.qe.timeguess.repositories;

import at.qe.timeguess.model.Category;

public interface CategoryRepository extends AbstractRepository<Category, Long> {

    // TODO: make sure Categories get ordered by ID

	Category findFirstByName(String name);



}
