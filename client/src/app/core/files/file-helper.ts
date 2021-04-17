export class FileHelper {
  /**
   * Reads the file and returns the data
   *
   * @param file
   */
  static readFile(file: File) {
    let reader = new FileReader();
    reader.readAsText(file, 'UTF-8');

    return new Promise<string>((res) => {
      reader.onload = function (event) {
        if (event === null || event.target === null) {
          throw new Error('Error loading file');
        }

        let content = event.target.result;

        if (typeof content !== 'string') {
          throw new Error('Error reading file');
        }

        res(content);
      };

      reader.onerror = function () {
        throw new Error('Error reading file');
      };
    });
  }

  /**
   * Parses the csv data and returns the resulting string-array
   *
   * @param csv_data The csv (separated by ;) data
   * @returns the parsed csv data
   */
  static parseCSV(csv_data: string) {
    let new_line_char = '\n';

    if (csv_data.includes('\r\n')) {
      new_line_char = '\r\n';
    }

    let import_data = csv_data.split(new_line_char).map((line) => {
      return line.split(';');
    });

    if (import_data.length === 0) {
      return [];
    }

    for (let i = 1; i < import_data.length; i++) {
      if (import_data[i].length != import_data[i - 1].length) {
        throw new Error('File format is invalid');
      }
    }

    return import_data;
  }
}
