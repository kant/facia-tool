import template from 'troubleshoot/templates/effort.html!text';
import {generateClone, wrap} from 'troubleshoot/lib/renderer';

const clone = generateClone(template);
const wrapper = wrap();
const model = {
    data: null
};
const DATA_PATH = 'https://s3-eu-west-1.amazonaws.com/auditing-store/front-curation-effort/whatever.json';

export function render (container) {
    const mainView = clone('mainView');
    wrapper.initialize(mainView, container);
    loadData(container);
    registerListeners(container);
}

export function update (container) {
    /* nothing to do */
    console.log('update');
}

export function dispose () {
    wrapper.dispose();
}

function registerListeners (container) {
    const searchField = container.querySelector('.searchField');
    wrapper.attach(searchField, 'input', search);
}

function loadData (container) {
    fetch(DATA_PATH)
        .then(resp => {
            if (resp.ok) {
                return resp.json();
            } else {
                return Promise.reject(new Error('Could not get data, ' + resp.statusText + ': ' + resp.status));
            }
        })
        .then(storeData)
        .then(() => refreshData(container))
        .catch(() => dataError(container));
}

function storeData (data) {
    model.data = data;
}

function refreshData (container) {
    console.log('refresh', container);
}

function dataError (container) {
    console.log('error', container);
}

function search (container) {
    console.log('search');
}
