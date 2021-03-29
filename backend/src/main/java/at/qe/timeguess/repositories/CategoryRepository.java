package at.qe.timeguess.repositories;

import at.qe.timeguess.model.Category;

public interface CategoryRepository extends AbstractRepository<Category, Long> {

	Category findFirstByCategory(String category);

	Category findFirstByid(Long iD);

}
