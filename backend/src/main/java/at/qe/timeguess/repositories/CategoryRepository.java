package at.qe.timeguess.repositories;

import at.qe.timeguess.model.Category;

public interface CategoryRepository extends AbstractRepository<Category, Long> {

	Category findFirstByName(String name);



}
