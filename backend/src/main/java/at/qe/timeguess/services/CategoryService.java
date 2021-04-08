package at.qe.timeguess.services;

import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.CompletedGame;
import at.qe.timeguess.repositories.CategoryRepository;
import at.qe.timeguess.repositories.CompletedGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Scope("application")
public class CategoryService {

    // TODO: add PreAuthorize-annotations where needed

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CompletedGameRepository completedGameRepository;

    public Collection<Category> getAllCategories() {
        return this.categoryRepository.findAll();
    }

    public Category getCategoryById(final Long id) {
        return this.categoryRepository.findFirstById(id);
    }

    public Category getCategoryByName(final String name) {
        return this.categoryRepository.findFirstByName(name);
    }

    public Category saveCategory(final Category category) throws CategoryAlreadyExistsException{

        if(this.getCategoryByName(category.getName()) != null) {
            throw new CategoryAlreadyExistsException("This Category already exists!");
        }
        return this.categoryRepository.save(category);
    }

    public void deleteCategory(final Category category) throws CategoryAlreadyExistsException {

        Collection<CompletedGame> allCompletedGames = this.completedGameRepository.findAll();

        if(allCompletedGames.stream().anyMatch(completedGame -> completedGame.getCategory().getName().equals(category.getName()))) {
            throw new CategoryAlreadyExistsException("This Category can not be deleted, because it is referenced in a completed game!");
        }
        this.categoryRepository.delete(category);
    }

    public class CategoryIsReferencedInCompletedGamesException extends Exception {

        private static final long serialVersionUID = 1L;

        public CategoryIsReferencedInCompletedGamesException(final String message) {
            super(message);
        }
    }

    public class CategoryAlreadyExistsException extends Exception {

        private static final long serialVersionUID = 1L;

        public CategoryAlreadyExistsException(final String message) {
            super(message);
        }
    }
}

