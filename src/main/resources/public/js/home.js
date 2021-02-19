window.onload = function() {
    init();
};

function init() {
    const editor = CodeMirror.fromTextArea(document.getElementById("code"), {
        lineNumbers: true,
        matchBrackets: true,
        mode: "text/x-groovy"
    });

    document.getElementById("clear-output").onclick = resetOutputSection;
    document.getElementById("clear-all").onclick = clearAll;
    document.getElementById("execute").onclick = executeCode;

    function executeCode() {
        console.log("Executing script");
        resetOutputSection();
        const requestData = { code: editor.getValue()  }
        const xhr = new XMLHttpRequest();
        xhr.open("POST", "/", true);
        xhr.setRequestHeader('Content-Type', 'application/json');
        xhr.send(JSON.stringify(requestData));
        xhr.onload = function() {
            const jsonResult = JSON.parse(this.responseText)
            document.getElementById("executionTime").innerText = jsonResult.executionTime;
            if(jsonResult.output) {
                document.getElementById("output").innerText = jsonResult.output;
                document.getElementById("error-card").hidden = true
                document.getElementById("output-card").hidden = false
            }
            if(jsonResult.error) {
                document.getElementById("error").innerText = jsonResult.error;
                document.getElementById("error-card").hidden = false
                document.getElementById("output-card").hidden = true
            }
        };

    }

    function clearAll() {
        console.log("Clearing code and output")
        editor.setValue("");
        editor.clearHistory();
        resetOutputSection();
    }

    function resetOutputSection() {
        document.getElementById("executionTime").innerText = "";
        document.getElementById("output").innerText = "";
        document.getElementById("error").innerText = "";
        document.getElementById("output-card").hidden = false;
        document.getElementById("error-card").hidden = true;
    }

    function keyPressShortcuts(e) {
        if(e.ctrlKey) {
            switch (e.keyCode) {
                case 82: // R
                    executeCode();
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

    document.onkeydown = keyPressShortcuts;
}