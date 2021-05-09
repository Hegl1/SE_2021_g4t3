package at.qe.timeguess.services;

import at.qe.timeguess.gamelogic.Game;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.CompletedGame;
import at.qe.timeguess.repositories.CategoryRepository;
import at.qe.timeguess.repositories.CompletedGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * This class represents the Service with which Categories get managed.
 * It is possible to create, delete Categories, and to find Categories by specific properties as well.
 *
 */
@Service
@Scope("application")
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CompletedGameRepository completedGameRepository;

    @Autowired
    private LobbyService lobbyService;

    /**
     * Returns all Categories
     *
     * @return all Categories from the database
     */
    public Collection<Category> getAllCategories() {
        return this.categoryRepository.findByOrderByIdAsc();
    }

    /**
     * Returns Category which is searched by a given ID
     *
     * @param id the ID which the Category is searched by
     * @return the Category found by the ID
     */
    public Category getCategoryById(final Long id) {
        return this.categoryRepository.findFirstById(id);
    }

    /**
     * Returns Category which is searched by a given name
     *
     * @param name the name which the Category is searched by
     * @return the Category found by the name
     */
    public Category getCategoryByName(final String name) {
        return this.categoryRepository.findFirstByName(name);
    }

    /**
     * Checks if a Category is deletable, based on occurrences in completed Games
     *
     * @param category the Category for which is checked if it is deletable
     * @return true or false, whether the Category is deletable
     */
    public boolean isDeletable(Category category) {
        Collection<CompletedGame> allCompletedGames = this.completedGameRepository.findAll();
        Collection<Game> allRunningGames = this.lobbyService.getAllRunningGames();

        return allCompletedGames.stream().noneMatch(completedGame -> completedGame.getCategory().getName().equals(category.getName())) ||
            allRunningGames.stream().anyMatch(runningGame -> runningGame.getCategory().getId().equals(category.getId()));
    }

    /**
     * Saves a new Category for Expressions
     *
     * @param category the Category to save
     * @return the Category that got saved in the database
     * @throws CategoryAlreadyExistsException if a Category to save already exists
     */
    public Category saveCategory(final Category category) throws CategoryAlreadyExistsException{
        if(this.getCategoryByName(category.getName()) != null) {
            throw new CategoryAlreadyExistsException("This Category already exists!");
        }
        return this.categoryRepository.save(category);
    }

    /**
     * Deletes a Category, except when it is referenced in the completed games
     *
     * @param category the Category to delete
     * @throws CategoryIsReferencedInCompletedGamesException if the Category to be deleted is referenced in the persisted completed games
     */
    public void deleteCategory(final Category category) throws CategoryIsReferencedInCompletedGamesException {
        Collection<CompletedGame> allCompletedGames = this.completedGameRepository.findAll();
        Collection<Game> allRunningGames = this.lobbyService.getAllRunningGames();

        if(!this.isDeletable(category)) {
            throw new CategoryIsReferencedInCompletedGamesException("This Category can not be deleted, because it is referenced in a game!");
        }
        this.categoryRepository.delete(category);
    }

    /**
     * Gets thrown when a Category is tried to be deleted, which is referenced in the persisted completed games
     */
    public static class CategoryIsReferencedInCompletedGamesException extends Exception {

        private static final long serialVersionUID = 1L;

        public CategoryIsReferencedInCompletedGamesException(final String message) {
            super(message);
        }
    }

    /**
     * Gets thrown when a Category is tried to be created and saved, which already exists in the database
     */
    public static class CategoryAlreadyExistsException extends Exception {

        private static final long serialVersionUID = 1L;

        public CategoryAlreadyExistsException(final String message) {
            super(message);
        }
    }
}

