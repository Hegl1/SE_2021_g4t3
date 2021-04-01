package at.qe.timeguess.controllers;

import at.qe.timeguess.model.Category;
import at.qe.timeguess.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RequestMapping("/categories")
@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    public ResponseEntity<Category> getAllCategories(){
        this.categoryService.getAllCategories();
        return new ResponseEntity<Category>(HttpStatus.OK);
    }

    // TODO: createCategory method

    public ResponseEntity<Category> deleteCategory(final Long id) {
        Category categoryToDelete = this.categoryService.getCategoryById(id);

        if(categoryToDelete != null) {
            this.categoryService.deleteCategory(categoryToDelete);
            return new ResponseEntity<Category>(HttpStatus.OK);
        } else {
            return new ResponseEntity<Category>(HttpStatus.NOT_FOUND);
        }
    }
}
