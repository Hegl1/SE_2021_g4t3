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

@RequestMapping("/categories")
@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("")
    public ResponseEntity<List<Category>> getAllCategories(){
        List<Category> allCategories = new LinkedList<>(this.categoryService.getAllCategories());
        return new ResponseEntity<List<Category>>(allCategories, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Category> createCategory(@RequestBody final Category category) {
        try {
            Category newCategory = this.categoryService.saveCategory(category);
            return new ResponseEntity<Category>(newCategory, HttpStatus.CREATED);
        } catch (CategoryService.CategoryAlreadyExistsException e) {
            return new ResponseEntity<Category>(HttpStatus.CONFLICT);
        }
    }

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
