const FEATURES = 'width=600,height=600';

function getSafeUrl() {
    return document.getElementById(ELEMENT_CODE_URL).value;
}

function shareOnFacebook() {
    window.open('http://www.facebook.com/share.php?u='+getSafeUrl(),'popup',FEATURES);
    return false;
}

function shareOnTwitter() {
    window.open('https://twitter.com/intent/tweet?url='+getSafeUrl()+'&text="New code sample"&via=Joxebus','popup',FEATURES);
    return false;
}

function shareOnLinkedIn() {
    window.open('https://www.linkedin.com/sharing/share-offsite/?url='+getSafeUrl(),'popup',FEATURES);
    return false;
}