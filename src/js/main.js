window.deps = {
    'react' : require('react'),
    'react-dom' : require('react-dom'),
    'slate' : require('slate'),
    'edittable': require('slate-edit-table'),
    'react-tinymce': require('react-tinymce')
};

console.log("slate", require('slate'));

window.React = window.deps['react'];
window.ReactDOM = window.deps['react-dom'];
