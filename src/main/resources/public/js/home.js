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
    document.getElementById("download").onclick = downloadCode;

    function executeCode() {
        console.log("Executing script");
        resetOutputSection();
        const requestData = { code: editor.getValue()  }
        const request = new XMLHttpRequest();
        request.open("POST", "/script/execute", true);
        request.setRequestHeader('Content-Type', 'application/json');
        request.send(JSON.stringify(requestData));
        request.onload = function() {

            const jsonResult = JSON.parse(this.responseText)
            document.getElementById("executionTime").innerText = jsonResult.executionTime;
            if(request.status === 200) {
                document.getElementById("output").innerText = jsonResult.output;
                document.getElementById("error-card").hidden = true
                document.getElementById("output-card").hidden = false
            } else {
                document.getElementById("error").innerText = jsonResult.error;
                document.getElementById("error-card").hidden = false
                document.getElementById("output-card").hidden = true
            }
        };

    }

    function downloadCode() {
        console.log("Downloading script");
        const requestData = { code: editor.getValue()  }
        const request = new XMLHttpRequest();
        request.open("POST", "/script/download", true);
        request.setRequestHeader('Content-Type', 'application/json');
        request.responseType = 'blob';
        request.send(JSON.stringify(requestData));

        request.onload = function() {
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

    document.onkeydown = keyPressShortcuts;
}