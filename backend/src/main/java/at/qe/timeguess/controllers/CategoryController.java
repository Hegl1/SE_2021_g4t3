package at.qe.timeguess.controllers;

import at.qe.timeguess.dto.CategoryInfoDTO;
import at.qe.timeguess.dto.NameDTO;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.repositories.CompletedGameRepository;
import at.qe.timeguess.services.CategoryService;
import at.qe.timeguess.services.ExpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that controls the managing of the Categories
 *
 */
@RequestMapping("/categories")
@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private CompletedGameRepository completedGameRepository;

    /**
     * Returns a List of all Categories in the database
     *
     * @return ResponseEntity for REST communication:
     *      code OK if successful
     */
    @GetMapping("")
    public ResponseEntity<List<Category>> getAllCategories(){
        List<Category> allCategories = new LinkedList<>(this.categoryService.getAllCategories());
        return new ResponseEntity<List<Category>>(allCategories, HttpStatus.OK);
    }

    /**
     * Retrieves all Categories with information regarding its id, name, deletableness and its amount of Expressions
     *
     * @return ResponseEntity for REST communication:
     *      code OK if successful
     */
    @GetMapping("/info")
    public ResponseEntity<List<CategoryInfoDTO>> getAllCategoriesWithInfo() {
        Collection<Category> allCategories = this.categoryService.getAllCategories();
        List<CategoryInfoDTO> allCategoriesWithInfo = new LinkedList<>();

        for(Category current : allCategories) {
            CategoryInfoDTO categoryInfoDTO = new CategoryInfoDTO(
                current.getId(),
                current.getName(),
                this.categoryService.isDeletable(current),
                this.expressionService.getAllExpressionsByCategory(current).size()
            );
            allCategoriesWithInfo.add(categoryInfoDTO);
        }
        return new ResponseEntity<List<CategoryInfoDTO>>(allCategoriesWithInfo, HttpStatus.OK);
    }

    /**
     * Returns a Category by its ID
     *
     * @param id the ID of the Category to be acquired
     * @return ResponseEntity for REST communication:
     *      code OK        if successful
     *      code NOT_FOUND if the category was not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable final Long id) {
        Category category = this.categoryService.getCategoryById(id);

        if(category != null) {
            return new ResponseEntity<Category>(category, HttpStatus.OK);
        } else {
            return new ResponseEntity<Category>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Creates a new Category
     *
     * @param nameDTO contains the name of the Category to be created
     * @return ResponseEntity for REST communication:
     *      code CREATED  if successful
     *      code CONFLICT if category with this name exists already
     */
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('GAMEMANAGER')")
    @PostMapping("")
    public ResponseEntity<Category> createCategory(@RequestBody final NameDTO nameDTO) {
        Category category = new Category(nameDTO.getName());

        try {
            Category newCategory = this.categoryService.saveCategory(category);
            return new ResponseEntity<Category>(newCategory, HttpStatus.CREATED);
        } catch (CategoryService.CategoryAlreadyExistsException e) {
            return new ResponseEntity<Category>(HttpStatus.CONFLICT);
        }
    }

    /**
     * Deletes an existing Category
     *
     * @param id the ID of the Category to be deleted
     * @return ResponseEntity for REST communication:
     *      code OK         if Category got deleted successfully
     *      code FORBIDDEN  if a Category is referenced in persisted completed Game
     *      code NOT_FOUND  if the Category
     */
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('GAMEMANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Category> deleteCategory(@PathVariable final Long id) {
        Category categoryToDelete = this.categoryService.getCategoryById(id);

        if(categoryToDelete != null) {
            try {
                this.categoryService.deleteCategory(categoryToDelete);
            } catch (CategoryService.CategoryIsReferencedInCompletedGamesException e) {
                return new ResponseEntity<Category>(HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<Category>(HttpStatus.OK);
        } else {
            return new ResponseEntity<Category>(HttpStatus.NOT_FOUND);
        }
    }
}
