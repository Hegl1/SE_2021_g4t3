import { AbstractControl, Validators } from '@angular/forms';

const CustomValidators = {
  requiredIfTrue: (expression: boolean | Function) => {
    return (control: AbstractControl) => {
      if (typeof expression === 'function') {
        if (!expression()) return null;
      } else {
        if (!expression) return null;
      }
      return Validators.required(control);
    };
  },
};

export default CustomValidators;
