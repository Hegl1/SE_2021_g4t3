package at.qe.timeguess.services;

import at.qe.timeguess.dto.CategoryExpressionAsStringsDTO;
import at.qe.timeguess.dto.CategoryExpressionDTO;
import at.qe.timeguess.dto.ExpressionDTO;
import at.qe.timeguess.dto.NameDTO;
import at.qe.timeguess.gamelogic.Game;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.CompletedGame;
import at.qe.timeguess.model.Expression;
import at.qe.timeguess.repositories.CompletedGameRepository;
import at.qe.timeguess.repositories.ExpressionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class represents the Service with which Expressions get managed.
 * It is possible to create, import with JSON file, delete Expressions,
 * and to find Expression by specific properties as well.
 *
 */
@Service
@Scope("application")
public class ExpressionService {

    // TODO: add PreAuthorize-annotations where needed

    @Autowired
    private ExpressionRepository expressionRepository;

    @Autowired
    private CompletedGameRepository completedGameRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LobbyService lobbyService;

    /**
     * Returns a Expression which is searched by an ID
     *
     * @param id the ID with which the expression get searched by
     * @return the expression with the corresponding ID
     */
    public Expression getExpressionById(final Long id) {
        return this.expressionRepository.findFirstById(id);
    }

    /**
     * Returns a Expression which is searched by its Category and name
     *
     * @param category the Category in which the Expression is in
     * @param name the name of the Expression
     * @return the Expression with corresponding name and Category
     */
    public Expression getExpressionByCategoryAndName(final Category category, final String name) {
        return this.expressionRepository.findFirstByCategoryAndName(category, name);
    }

    /**
     * Returns all Expressions in the database
     *
     * @return all Expressions
     */
    public Collection<Expression> getAllExpressions() {
        return this.expressionRepository.findAll();
    }

    /**
     * Returns all Expressions of a given Category
     *
     * @param category the Category of which all the Expressions are searched for
     * @return all the Expressions of a given Category
     */
    public Collection<Expression> getAllExpressionsByCategory(final Category category) {
        return this.expressionRepository.findByCategory(category);
    }

    /**
     * Return a random Expression of a Category
     *
     * @param category the Category from which an Expression gets returned
     * @return a random Expression of a Category
     */
    public Expression getRandomExpressionByCategory(final Category category) {
        Collection<Expression> allExpressionsByCategory = this.getAllExpressionsByCategory(category);
        int random = (int) (Math.random() * allExpressionsByCategory.size());

        for(Expression current: allExpressionsByCategory) {
            if(--random < 0) {
                return current;
            }
        }
        throw new AssertionError();
    }

    /**
     * Saves an Expression in the database
     *
     * @param categoryId the ID of the Category to which the Expression gets assigned
     * @param nameDTO contains the name of the Expression
     * @return an ExpressionDTO with the information of the saved Expression
     * @throws ExpressionAlreadyExists if the Expression already exists in the Category
     */
    public ExpressionDTO saveExpression(final Long categoryId, final NameDTO nameDTO) throws ExpressionAlreadyExists {
        Category category = this.categoryService.getCategoryById(categoryId);
        Collection<Expression> allExpressionsOfCategory = getAllExpressionsByCategory(category);

        if(allExpressionsOfCategory.stream().anyMatch(e -> e.getName().equals(nameDTO.getName()))) {
            throw new ExpressionAlreadyExists("This Expression already exists in this Category!");
        }
        Expression expression = this.expressionRepository.save(new Expression(nameDTO.getName(), category));
        return new ExpressionDTO(expression.getId(), expression.getName());
    }

    /**
     *  Imports Expressions into the database and assigns them to a given Category,
     *  if there Expressions to import, which already exist in the database, they get ignored,
     *  thus no duplicates are being created
     *
     * @param categoryId the ID of the Category to which the Expressions get assigned
     * @param expressionNames the names of the Expressions to be imported
     * @return the Collection of the imported Expressions
     */
    public List<ExpressionDTO> importExpressionsIntoCategory(final Long categoryId, final Collection<String> expressionNames) throws ExpressionAlreadyExists {
        Category category = this.categoryService.getCategoryById(categoryId);
        List<ExpressionDTO> expressionDTOs = new LinkedList<>();

        for(String current : expressionNames) {
            NameDTO nameDTO = new NameDTO(current);
            ExpressionDTO expressionDTO = this.saveExpression(categoryId, nameDTO);
            expressionDTOs.add(expressionDTO);
        }
        return expressionDTOs;
    }

    /**
     *  Imports Expressions of multiple Categories and creates Categories if required Category does not exist already
     *
     * @param categoryExpressionAsStringsDTOs Pairs of (Category, Expression[]) to get imported
     * @throws CategoryService.CategoryAlreadyExistsException if the Category to get created already exists
     */
    public List<CategoryExpressionDTO> importExpressions(final Collection<CategoryExpressionAsStringsDTO> categoryExpressionAsStringsDTOs) throws CategoryService.CategoryAlreadyExistsException, ExpressionAlreadyExists {
        Category category;
        List<String> expressionNames = new LinkedList<>();
        List<ExpressionDTO> expressionDTOs = new LinkedList<>();
        List<CategoryExpressionDTO> categoryExpressionDTOs = new LinkedList<>();

        for (CategoryExpressionAsStringsDTO current : categoryExpressionAsStringsDTOs) {
            category = this.categoryService.getCategoryByName(current.getCategory());
            if(category == null) {
                category = this.categoryService.saveCategory(new Category(current.getCategory()));
            }

            expressionNames.addAll(current.getExpressions());
            expressionDTOs = this.importExpressionsIntoCategory(category.getId(), expressionNames);
            categoryExpressionDTOs.add(new CategoryExpressionDTO(category, expressionDTOs));
        }
        return categoryExpressionDTOs;
    }

    /**
     * Deletes an Expression
     *
     * @param expression the Expression to be deleted
     * @throws ExpressionDoesNotExistAnymore if the Expression to get deleted does not exist anymore
     */
    public void deleteExpression(final Expression expression) throws ExpressionDoesNotExistAnymore, ExpressionReferencedInGame {

        if(expression == null) {
            throw new ExpressionDoesNotExistAnymore("This Expression does not exist anymore!");
        } else {
            Category category = expression.getCategory();
            Collection<Game> allRunningGames = this.lobbyService.getAllRunningGames();
            Collection<CompletedGame> allCompletedGames = this.completedGameRepository.findAll();

            for(Game current : allRunningGames) {

                if(current.getCategory() == category) {
                    throw new ExpressionReferencedInGame("The Category of this Expression is referenced in a Game!");
                }
            }

            for(CompletedGame current : allCompletedGames) {

                if(current.getCategory().getId() == category.getId()) {
                    throw new ExpressionReferencedInGame("The Category of this Expression is referenced in a Game!");
                }
            }
            this.expressionRepository.delete(expression);
        }
    }

    /**
     * Gets thrown when an Expression is tried to be created, which already exists
     */
    public class ExpressionAlreadyExists extends Exception {

        private static final long serialVersionUID = 1L;

        public ExpressionAlreadyExists(final String message) {
            super(message);
        }
    }

    /**
     * Gets thrown when an Expression is tried to be deleted, which does not exist anymore
     */
    public class ExpressionDoesNotExistAnymore extends Exception {

        private static final long serialVersionUID = 1L;

        public ExpressionDoesNotExistAnymore(final String message) {
            super(message);
        }
    }

    /**
     * Gets thrown when an Expression is tried to be deleted, which referenced in a completed game
     */
    public class ExpressionReferencedInGame extends Exception {
        private static final long serialVersionUID = 1L;

        public ExpressionReferencedInGame(final String message) {
            super(message);
        }
    }
}
