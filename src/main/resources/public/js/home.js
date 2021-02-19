window.onload = function() {
    init();
};

function init() {
    const editor = CodeMirror.fromTextArea(document.getElementById("code"), {
        lineNumbers: true,
        matchBrackets: true,
        mode: "text/x-groovy"
    });

    document.getElementById("clear").onclick = function() {
        console.log("Clearing code and output")
        editor.setValue("");
        editor.clearHistory();
        resetOutputSection();
    }

    document.getElementById("execute").onclick = function() {
        console.log("Executing script");
        resetOutputSection();
        executeCode(editor.getValue());
    }

    function executeCode(code) {
        const requestData = { code: code  }
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

    function resetOutputSection() {
        document.getElementById("output").innerText = "";
        document.getElementById("error").innerText = "";
        document.getElementById("output-card").hidden = false;
        document.getElementById("error-card").hidden = true;
    }
}