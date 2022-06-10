// Themes
const THEME_LIGHT_MODE = "idea";
const THEME_DARK_MODE = "blackboard";

// Common element ids
const ELEMENT_CODE_URL = "code-url";
const ELEMENT_CODE_EDITOR = "code";
const ELEMENT_EXECUTION_TIME = "executionTime";
const ELEMENT_OUTPUT = "output";
const ELEMENT_ERROR_CARD = "error-card";
const ELEMENT_OUTPUT_CARD = "output-card";
const ELEMENT_URL_HELP = "url-help";
const ELEMENT_SHOW_MODAL = "show-modal";
const ELEMENT_ERROR = "error";
const ELEMENT_SPINNER = "spinner";

const ELEMENT_BTN_LIGHT_MODE = "light-mode";
const ELEMENT_BTN_DARK_MODE = "dark-mode";
const ELEMENT_BTN_CLEAR_OUTPUT = "clear-output";
const ELEMENT_BTN_CLEAR_ALL = "clear-all";
const ELEMENT_BTN_EXECUTE = "execute";
const ELEMENT_BTN_DOWNLOAD = "download";
const ELEMENT_BTN_UPLOAD = "upload-share-code";
const ELEMENT_BTN_COPY_TO_CLIPBOARD = "copy-url";

// Local storage
const KEY_GROOVY_CODE = "groovyCode";
const KEY_THEME = "theme";

// Time in minutes
const MINUTES = 5;
const SAVE_INTERVAL_MINUTES = MINUTES * 60000;

// Constants
const EMPTY_STRING = "";
const UNDEFINED_STRING = "undefined";
const MEDIA_TYPE_JSON = 'application/json';
const HEADER_CONTENT_TYPE = 'Content-Type';

window.onload = function() {
    init();
};

function init() {

    if (typeof(Storage) !== UNDEFINED_STRING) {
        console.log("Automatic save every 5 minutes on local storage enabled");
        const interval = setInterval(function() {
            saveCodeInLocalStorage()
        }, SAVE_INTERVAL_MINUTES);

        const code = document.getElementById(ELEMENT_CODE_EDITOR).value
        if(code === "// Write your code here") {
            restoreCodeFromLocalStorage();
        }
    } else {
        console.log("Your browser does not support local storage")
    }

    const editor = CodeMirror.fromTextArea(document.getElementById(ELEMENT_CODE_EDITOR), {
        styleActiveLine: true,
        matchBrackets: true,
        autoCloseBrackets: true,
        lineNumbers: true,
        mode: "text/x-groovy"
    });

    restoreThemeFromLocalStorage();

    document.getElementById(ELEMENT_BTN_LIGHT_MODE).onclick = setLightMode;
    document.getElementById(ELEMENT_BTN_DARK_MODE).onclick = setDarkMode;
    document.getElementById(ELEMENT_BTN_CLEAR_OUTPUT).onclick = resetOutputSection;
    document.getElementById(ELEMENT_BTN_CLEAR_ALL).onclick = clearAll;
    document.getElementById(ELEMENT_BTN_EXECUTE).onclick = executeCode;
    document.getElementById(ELEMENT_BTN_DOWNLOAD).onclick = downloadCode;
    document.getElementById(ELEMENT_BTN_UPLOAD).onclick = uploadCode;
    document.getElementById(ELEMENT_BTN_COPY_TO_CLIPBOARD).onclick = copyUrlToClipboard;

    function setDarkMode() {
        selectTheme(THEME_DARK_MODE, true);
    }

    function setLightMode() {
        selectTheme(THEME_LIGHT_MODE, false);
    }

    function selectTheme(theme, isDarkMode) {
        editor.setOption(KEY_THEME, theme);
        document.getElementById(ELEMENT_BTN_LIGHT_MODE).hidden = !isDarkMode;
        document.getElementById(ELEMENT_BTN_DARK_MODE).hidden = isDarkMode;
        if (typeof(Storage) !== UNDEFINED_STRING) {
            console.log("Saving theme ["+ theme +"] in local storage");
            localStorage.setItem(KEY_THEME, theme);
        }
    }

    function executeCode() {
        console.log("Executing script");
        const timeoutId = showSpinner();
        saveCodeInLocalStorage();
        resetOutputSection();
        const requestData = { code: editor.getValue()  }
        const request = new XMLHttpRequest();
        request.open("POST", "/script/execute", true);
        request.setRequestHeader(HEADER_CONTENT_TYPE, MEDIA_TYPE_JSON);
        request.send(JSON.stringify(requestData));
        request.onload = function() {
            hideSpinner(timeoutId);
            const jsonResult = JSON.parse(this.responseText)
            document.getElementById(ELEMENT_EXECUTION_TIME).innerText = jsonResult.executionTime;
            if(request.status === 200) {
                document.getElementById(ELEMENT_OUTPUT).innerText = jsonResult.output;
                document.getElementById(ELEMENT_ERROR_CARD).hidden = true
                document.getElementById(ELEMENT_OUTPUT_CARD).hidden = false
            } else {
                document.getElementById(ELEMENT_ERROR).innerText = jsonResult.error;
                document.getElementById(ELEMENT_ERROR_CARD).hidden = false
                document.getElementById(ELEMENT_OUTPUT_CARD).hidden = true
            }
        };

    }

    function uploadCode() {
        console.log("Uploading script");
        const timeoutId = showSpinner();
        saveCodeInLocalStorage();
        resetOutputSection();
        const requestData = { code: editor.getValue()  }
        const request = new XMLHttpRequest();
        request.open("POST", "/script/upload", true);
        request.setRequestHeader(HEADER_CONTENT_TYPE, MEDIA_TYPE_JSON);
        request.send(JSON.stringify(requestData));
        request.onload = function() {
            hideSpinner(timeoutId);
            const jsonResult = JSON.parse(this.responseText)
            console.log(this.responseText)

            if(request.status === 200) {
                takeScreenshot(jsonResult.url);
                document.getElementById(ELEMENT_CODE_URL).value = jsonResult.url;
                document.getElementById(ELEMENT_OUTPUT).innerText = "Code uploaded!";
                document.getElementById(ELEMENT_ERROR_CARD).hidden = true
                document.getElementById(ELEMENT_OUTPUT_CARD).hidden = false
                document.getElementById(ELEMENT_URL_HELP).hidden = true
                document.getElementById(ELEMENT_SHOW_MODAL).click();
            } else {
                document.getElementById(ELEMENT_ERROR).innerText = jsonResult.error.message;
                document.getElementById(ELEMENT_ERROR_CARD).hidden = false
                document.getElementById(ELEMENT_OUTPUT_CARD).hidden = true
            }
        };

    }

    function downloadCode() {
        console.log("Downloading script");
        const timeoutId = showSpinner();
        saveCodeInLocalStorage();
        const requestData = { code: editor.getValue()  }
        const request = new XMLHttpRequest();
        request.open("POST", "/script/download", true);
        request.setRequestHeader(HEADER_CONTENT_TYPE, MEDIA_TYPE_JSON);
        request.responseType = 'blob';
        request.send(JSON.stringify(requestData));

        request.onload = function() {
            hideSpinner(timeoutId);
            // Only handle status code 200
            if(request.status === 200) {
                // Try to find out the filename from the content disposition `filename` value
                let disposition = request.getResponseHeader('content-disposition');
                let matches = /"([^"]*)"/.exec(disposition);
                let filename = (matches != null && matches[1] ? matches[1] : 'code.groovy');

                // The actual download
                let blob = new Blob([request.response], { type: 'text/x-groovy' });
                let link = document.createElement('a');
                link.href = window.URL.createObjectURL(blob);
                link.download = filename;

                document.body.appendChild(link);

                link.click();

                document.body.removeChild(link);
            }
        }
    }

    function copyUrlToClipboard() {
        const url = document.getElementById(ELEMENT_CODE_URL).value
        document.getElementById(ELEMENT_URL_HELP).hidden = false
        navigator.clipboard.writeText(url);
    }

    function clearAll() {
        console.log("Clearing code and output");
        editor.setValue(EMPTY_STRING);
        editor.clearHistory();
        resetOutputSection();
        clearLocalStorage();
    }

    function resetOutputSection() {
        document.getElementById(ELEMENT_EXECUTION_TIME).innerText = EMPTY_STRING;
        document.getElementById(ELEMENT_OUTPUT).innerText = EMPTY_STRING;
        document.getElementById(ELEMENT_ERROR).innerText = EMPTY_STRING;
        document.getElementById(ELEMENT_OUTPUT_CARD).hidden = false;
        document.getElementById(ELEMENT_ERROR_CARD).hidden = true;
    }

    function saveCodeInLocalStorage() {
        if (typeof(Storage) !== UNDEFINED_STRING) {
            console.log("Saving code in local storage");
            localStorage.setItem(KEY_GROOVY_CODE, JSON.stringify(editor.getValue().toString().split("\n")));
        }
    }

    function restoreThemeFromLocalStorage() {
        if (typeof(Storage) !== UNDEFINED_STRING) {
            const theme = localStorage.getItem(KEY_THEME);
            const isDarkModeEnabled = theme === THEME_DARK_MODE;
            if(theme) {
                console.log("Restoring theme "+theme+" from local storage")
                selectTheme(theme, isDarkModeEnabled);
            } else {
                selectTheme(THEME_LIGHT_MODE, false);
            }

        } else {
            selectTheme(THEME_LIGHT_MODE, false);
        }
    }

    function takeScreenshot(url) {
        let codeArea = document.getElementById('code-area');
        codeArea.style.width = "800px";
        codeArea.style.height = "400px";

        // Use the html2canvas
        // function to take a screenshot
        // and append it
        // to the output div
        html2canvas(codeArea).then(
            function (canvas) {
                codeArea.removeAttribute("style");
                canvas.toBlob(function(screenShot) {

                    let lastSlashIndex = url.lastIndexOf("/")+1
                    let filename = url.substring(lastSlashIndex) + ".png"
                    let request = new XMLHttpRequest();
                    let formData = new FormData();

                    formData.append("file", screenShot, filename);
                    request.open("POST", '/script/upload/image');
                    request.send(formData);
                });

            })
    }

    function getBrowserSize(){
        let w, h;

        if(typeof window.innerWidth != UNDEFINED_STRING) {
            w = window.innerWidth; //other browsers
            h = window.innerHeight;
        }
        else if(typeof document.documentElement != UNDEFINED_STRING
                && typeof document.documentElement.clientWidth != UNDEFINED_STRING
                && document.documentElement.clientWidth !== 0) {
            w =  document.documentElement.clientWidth; //IE
            h = document.documentElement.clientHeight;
        } else {
            w = document.body.clientWidth; //IE
            h = document.body.clientHeight;
        }
        return {'width':w, 'height': h};
    }

    function restoreCodeFromLocalStorage() {
        if (typeof(Storage) !== UNDEFINED_STRING) {
            const code = localStorage.getItem(KEY_GROOVY_CODE);
            if(code) {
                console.log("Restoring code from local storage");
                document.getElementById(ELEMENT_CODE_EDITOR).innerHTML = JSON.parse(code).join("\n");
            }

        }
    }

    function clearLocalStorage() {
        if (typeof(Storage) !== UNDEFINED_STRING) {
            localStorage.removeItem(KEY_GROOVY_CODE)
        }
    }

    function keyPressShortcuts(e) {
        if(e.ctrlKey) {
            switch (e.keyCode) {
                case 82: // R
                    executeCode();
                    break;
                case 83: // S
                    downloadCode();
                    break;
                case 87: // W
                    resetOutputSection();
                    break;
                case 75: // K
                    clearAll();
                    break;
            }
        }
    }

    function showSpinner() {
        return setTimeout(function () {
            document.getElementById(ELEMENT_SPINNER).style.display = "block";
        }, 1000);

    }

    function hideSpinner(spinnerTimeoutId) {
        clearTimeout(spinnerTimeoutId);
        document.getElementById(ELEMENT_SPINNER).style.display = "none";
    }

    document.onkeydown = keyPressShortcuts;
}