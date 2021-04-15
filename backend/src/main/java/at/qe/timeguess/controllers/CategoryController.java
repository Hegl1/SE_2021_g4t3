package at.qe.timeguess.controllers;

import at.qe.timeguess.model.Category;
import at.qe.timeguess.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /**
     * Returns a List of all Categories in the database
     *
     * @return ResponseEntity for REST communication (code 200 if successful)
     */
    @GetMapping("")
    public ResponseEntity<List<Category>> getAllCategories(){
        List<Category> allCategories = new LinkedList<>(this.categoryService.getAllCategories());
        return new ResponseEntity<List<Category>>(allCategories, HttpStatus.OK);
    }

    /**
     * Creates a new Category
     *
     * @param category the Category to be created
     * @return ResponseEntity for REST communication(code 201 if successful,
     * code 409 if category with this name exists already)
     */
    @PostMapping("")
    public ResponseEntity<Category> createCategory(@RequestBody final Category category) {
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
     * @return ResponseEntity for REST communication (code
     */
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