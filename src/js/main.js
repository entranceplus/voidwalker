window.deps = {
    'react' : require('react'),
    'react-dom' : require('react-dom'),
    'slate' : require('slate')
};

console.log("slate", require('slate'));

window.React = window.deps['react'];
window.ReactDOM = window.deps['react-dom'];
