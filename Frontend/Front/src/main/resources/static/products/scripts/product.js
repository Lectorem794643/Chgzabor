function clearForm() {
    // Находим форму по ID
    const form = document.getElementById('form');

    // Сбрасываем значения всех полей к значениям по умолчанию
    form.reset();
}

// Парсит данные полученные form от пользователя в формат DataModel для отправки в backend
function parsingHtmlFormJson() {
    const form = document.getElementById('form');
    const elements = form.elements;
    const values = {};

    // Соответствует HTML форме -------|
    values.id = "SlidingOptima001"; // |
    // --------------------------------|

    for (let i = 0; i < elements.length; i++) {
        const element = elements[i];
        if (element.name) {
            values[element.name] = element.value;
        }
    }

    return JSON.stringify(values);
}

// Получает данные с формы, отправляет на сервер, обрабатывает ответ и отображает результат на форме.
function processFormDataAndDisplayResults(event) {
    event.preventDefault(); // Браузер не перезагружает страницу

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "http://localhost:8000/api/SlidingOptima001/calculating", true);
    xhr.setRequestHeader("Content-Type", "application/json");

    xhr.send(parsingHtmlFormJson());
    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status === 200) {
                var response = JSON.parse(xhr.responseText);
                Object.keys(response).forEach(function (key) {
                    var element = document.getElementsByName(key)[0];
                    if (element) {
                        if (element.type === 'number') {
                            element.value = Number(response[key]);
                        } else {
                            element.value = response[key];
                        }
                    }
                });
            } else {
                alert("Error: " + xhr.status);
                console.error("Error: " + xhr.status);
            }
        }
    }
}

// Генерирует и скачивает PDF файл с чертежом из конструктора.
function downloadDrawingPDF(event) {
    event.preventDefault(); // Браузер не перезагружает страницу

    var xhr = new XMLHttpRequest();
    xhr.open('POST', "http://localhost:8000/api/SlidingOptima001/pdf", true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    xhr.responseType = 'blob';

    xhr.onload = function () {
        if (xhr.status === 200) {
            var blob = xhr.response;
            var url = window.URL.createObjectURL(blob);
            var a = document.createElement('a');
            a.href = url;
            a.download = document.getElementById("drawingName").value + '.pdf';
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
        } else {
            alert('Произошла ошибка при загрузке PDF файла');
            console.error('Произошла ошибка при загрузке PDF файла');
        }
    };

    xhr.onerror = function () {
        alert('Произошла ошибка при выполнении запроса');
        console.error('Произошла ошибка при выполнении запроса');
    };

    xhr.send(parsingHtmlFormJson());
}