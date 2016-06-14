export function generateClone (mainTemplateText) {
    const templatesMap = {};
    const templateElement = document.createElement('div');
    templateElement.innerHTML = mainTemplateText;

    return function (id) {
        if (!templatesMap[id]) {
            templatesMap[id] = templateElement.querySelector('#' + id);
            if (!templatesMap[id]) {
                throw new Error('Invalid template ID ' + id);
            }
        }
        return document.importNode(templatesMap[id].content, true);
    };
}

export function wrap () {
    let container;
    const disposeActions = [];

    const chain = {
        initialize (view, initialContainer) {
            container = initialContainer;
            container.innerHTML = '';
            container.appendChild(view);

            return chain;
        },
        dispose () {
            disposeActions.forEach(action => action());
            disposeActions.length = 0;
        },
        attach (element, event, callback, params = []) {
            const boundCallback = callback.bind(null, container, ...params);
            element.addEventListener(event, boundCallback);
            disposeActions.push(() => element.removeEventListener(event, boundCallback));

            return chain;
        }
    };

    return chain;
}
