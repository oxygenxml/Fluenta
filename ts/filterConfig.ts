/*******************************************************************************
 * Copyright (c) 2015-2025 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

class FilterConfig {

    electron = require('electron');
    selected: Map<string, any>;
    filterName: string;

    constructor() {
        this.electron.ipcRenderer.send('get-theme');
        this.electron.ipcRenderer.on('set-theme', (event: Electron.IpcRendererEvent, theme: string) => {
            (document.getElementById('theme') as HTMLLinkElement).href = theme;
        });
        document.addEventListener('keydown', (event: KeyboardEvent) => {
            if (event.code === 'Escape') {
                this.electron.ipcRenderer.send('close-filterConfig');
            }
        });
        this.electron.ipcRenderer.on('set-filterData', (event: Electron.IpcRendererEvent, arg: any) => {
            this.populateTable(arg);
        });
        document.getElementById('add').addEventListener('click', () => { this.addElement(); });
        document.getElementById('edit').addEventListener('click', () => { this.editElement(); });
        document.getElementById('remove').addEventListener('click', () => { this.removeElements(); });
        setTimeout(() => {
            this.electron.ipcRenderer.send('set-height', { window: 'filterConfig', width: document.body.clientWidth, height: document.body.clientHeight });
        }, 200);
    }

    populateTable(arg: any): void {
        this.filterName = arg.filter;
        let children: any[] = arg.children;
        let tbody: HTMLTableSectionElement = document.getElementById('tbody') as HTMLTableSectionElement;
        tbody.innerHTML = '';
        this.selected = new Map<string, any>();
        for (let tag of children) {
            let attributes: string[][] = tag.attributes;
            let tr: HTMLTableRowElement = document.createElement('tr');
            tbody.appendChild(tr);

            let td: HTMLTableCellElement = document.createElement('td');
            let checkbox: HTMLInputElement = document.createElement('input');
            checkbox.type = 'checkbox';
            td.appendChild(checkbox);
            tr.appendChild(td);

            td = document.createElement('td');
            td.innerText = tag.content;
            tr.appendChild(td);

            td = document.createElement('td');
            td.innerText = this.getAttribute(attributes, 'hard-break', 'segment');
            tr.appendChild(td);

            td = document.createElement('td');
            td.innerText = this.getAttribute(attributes, 'ctype', '');
            tr.appendChild(td);

            td = document.createElement('td');
            td.innerText = this.getAttribute(attributes, 'attributes', '');
            tr.appendChild(td);

            td = document.createElement('td');
            let keep = this.getAttribute(attributes, 'keep-format', '');
            td.innerText = keep ? arg.yes : ' ';
            tr.appendChild(td);

            tr.addEventListener('click', () => {
                this.clicked(tr, tag, checkbox);
            });
        }
    }

    clicked(row: HTMLTableRowElement, element: any, checkbox: HTMLInputElement): void {
        let isSelected: boolean = this.selected.has(element.content);
        if (!isSelected) {
            this.selected.set(element.content, element);
        } else {
            this.selected.delete(element.content);
        }
        checkbox.checked = !isSelected;
    }

    addElement(): void {
        let empty: ElementConfiguration = { filter: this.filterName, name: '', type: 'segment', inline: '', attributes: '', keepSpace: '' };
        this.electron.ipcRenderer.send('add-element', empty);
    }

    editElement(): void {
        if (this.selected.size === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'filterConfigurationDialog', key: 'selectElement' });
            return;
        }
        if (this.selected.size !== 1) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'filterConfigurationDialog', key: 'selectOneElement' });
            return;
        }
        let it: IterableIterator<[string, any]> = this.selected.entries();
        let first: IteratorResult<[string, any]> = it.next();
        let element: any = this.selected.get(first.value[0]);
        this.electron.ipcRenderer.send('add-element', {
            filter: this.filterName,
            name: element.content,
            type: this.getAttribute(element.attributes, 'hard-break', 'segment'),
            inline: this.getAttribute(element.attributes, 'ctype', ''),
            attributes: this.getAttribute(element.attributes, 'attributes', ''),
            keepSpace: this.getAttribute(element.attributes, 'keep-format', '')
        });
    }

    removeElements(): void {
        if (this.selected.size === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'filterConfigurationDialog', key: 'selectElement' });
            return;
        }
        let elements: string[] = [];
        this.selected.forEach((value: any, key: string) => {
            elements.push(key);
        });
        this.electron.ipcRenderer.send('remove-elements', { filter: this.filterName, elements: elements });
    }

    getAttribute(attributes: string[][], name: string, defaultValue: string): string {
        for (let attribute of attributes) {
            if (attribute[0] === name) {
                return attribute[1];
            }
        }
        return defaultValue;
    }
}
