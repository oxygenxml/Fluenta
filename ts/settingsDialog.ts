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

class SettingsDialog {

    electron = require('electron');
    tgtLangs: LanguageInterface[] = [];
    selecteCatalogs: string[] = [];
    selectedFilters: string[] = [];
    dialogWidth: number;
    removeText: string = '';

    constructor() {
        this.electron.ipcRenderer.send('get-theme');
        this.electron.ipcRenderer.on('set-theme', (event: Electron.IpcRendererEvent, theme: string) => {
            (document.getElementById('theme') as HTMLLinkElement).href = theme;
            this.electron.ipcRenderer.send('get-languages');
            this.electron.ipcRenderer.send('get-preferences');
        });
        this.electron.ipcRenderer.on('set-preferences', (event: Electron.IpcRendererEvent, arg: Preferences) => {
            this.setPreferences(arg);
        });
        document.addEventListener('keydown', (event: KeyboardEvent) => {
            if (event.code === 'Escape') {
                this.electron.ipcRenderer.send('close-settingsDialog');
            }
        });
        this.electron.ipcRenderer.on('set-languages', (event: Electron.IpcRendererEvent, languages: LanguageInterface[]) => {
            this.setLanguages(languages);
        });
        this.electron.ipcRenderer.on('set-default-languages', (event: Electron.IpcRendererEvent, arg: { srcLang: LanguageInterface, tgtLangs: LanguageInterface[], removeText: string }) => {
            this.setDefaultLanguages(arg);
            setTimeout(() => {
                this.dialogWidth = document.body.clientWidth;
                this.electron.ipcRenderer.send('set-height', { window: 'settingsDialog', width: this.dialogWidth, height: document.body.clientHeight });
            }, 300);
        });
        document.getElementById('browseProjectsButton').addEventListener('click', () => {
            this.electron.ipcRenderer.send('browse-projects-folder');
        });
        document.getElementById('browseMemoriesButton').addEventListener('click', () => {
            this.electron.ipcRenderer.send('browse-memories-folder');
        });
        document.getElementById('browseSrxButton').addEventListener('click', () => {
            this.electron.ipcRenderer.send('browse-srx-file');
        });
        document.getElementById('generalButton').addEventListener('click', () => {
            document.getElementById('generalButton').classList.add('selectedTab');
            document.getElementById('xmlButton').classList.remove('selectedTab');
            document.getElementById('generalOptions').classList.remove('hidden');
            document.getElementById('xmlOptions').classList.add('hidden');
            setTimeout(() => {
                this.electron.ipcRenderer.send('set-height', { window: 'settingsDialog', width: this.dialogWidth, height: document.body.clientHeight });
            }, 300);
        });
        document.getElementById('xmlButton').addEventListener('click', () => {
            document.getElementById('generalButton').classList.remove('selectedTab');
            document.getElementById('xmlButton').classList.add('selectedTab');
            document.getElementById('generalOptions').classList.add('hidden');
            document.getElementById('xmlOptions').classList.remove('hidden');
            this.electron.ipcRenderer.send('get-xml-options');
        });
        this.electron.ipcRenderer.on('set-xml-options', (event: Electron.IpcRendererEvent, arg: { filterFiles: string[], catalogEntries: string[] }) => {
            this.setXmlOptions(arg);
            setTimeout(() => {
                this.electron.ipcRenderer.send('set-height', { window: 'settingsDialog', width: this.dialogWidth, height: document.body.clientHeight });
            }, 300);
        });
        document.getElementById('addTarget').addEventListener('click', () => {
            this.electron.ipcRenderer.send('add-target-language', 'settingsDialog');
        });
        this.electron.ipcRenderer.on('add-language', (event: Electron.IpcRendererEvent, arg: LanguageInterface) => {
            let filtered: LanguageInterface[] = this.tgtLangs.filter((lang: LanguageInterface) => {
                return lang.code === arg.code;
            });
            if (filtered.length === 0) {
                this.tgtLangs.push(arg);
                this.displayTargetLanguages();
            }
        });
        document.getElementById('addCatalog').addEventListener('click', () => {
            this.electron.ipcRenderer.send('add-catalog');
        });
        document.getElementById('removeCatalog').addEventListener('click', () => {
            this.removeCatalog();
        });
        document.getElementById('addConfiguration').addEventListener('click', () => {
            this.electron.ipcRenderer.send('show-addConfigurationDialog');
        });
        document.getElementById('editConfiguration').addEventListener('click', () => {
            this.editFilter();
        });
        document.getElementById('removeConfiguration').addEventListener('click', () => {
            this.removeFilters();
        });
        document.getElementById('saveButton').addEventListener('click', () => {
            this.savePreferences();
        });
    }

    setXmlOptions(arg: { filterFiles: string[]; catalogEntries: string[]; }) {
        let configBody: HTMLTableSectionElement = document.getElementById('configBody') as HTMLTableSectionElement;
        configBody.innerHTML = '';
        for (let file of arg.filterFiles) {
            let row: HTMLTableRowElement = document.createElement('tr');
            configBody.appendChild(row);
            let cell: HTMLTableCellElement = document.createElement('td');
            let checkBox: HTMLInputElement = document.createElement('input');
            checkBox.type = 'checkbox';
            checkBox.id = file;
            cell.appendChild(checkBox);
            row.appendChild(cell);
            cell = document.createElement('td');
            let label: HTMLLabelElement = document.createElement('label');
            label.htmlFor = file;
            label.innerText = file;
            cell.appendChild(label);
            cell.classList.add('fill_width');
            row.appendChild(cell);
            checkBox.addEventListener('input', () => {
                if (checkBox.checked) {
                    this.selectedFilters.push(file);
                } else {
                    let index: number = this.selectedFilters.indexOf(file);
                    this.selectedFilters.splice(index, 1);
                }
            });
        }
        let catalogBody: HTMLTableSectionElement = document.getElementById('catalogBody') as HTMLTableSectionElement;
        catalogBody.innerHTML = '';
        for (let entry of arg.catalogEntries) {
            let row: HTMLTableRowElement = document.createElement('tr');
            catalogBody.appendChild(row);
            let cell: HTMLTableCellElement = document.createElement('td');
            let checkBox: HTMLInputElement = document.createElement('input');
            checkBox.id = entry;
            checkBox.type = 'checkbox';
            cell.appendChild(checkBox);
            row.appendChild(cell);
            cell = document.createElement('td');
            let label: HTMLLabelElement = document.createElement('label');
            label.htmlFor = entry;
            label.innerText = entry;
            cell.appendChild(label);
            cell.classList.add('fill_width');
            row.appendChild(cell);
            checkBox.addEventListener('input', () => {
                if (checkBox.checked) {
                    this.selecteCatalogs.push(entry);
                } else {
                    let index: number = this.selecteCatalogs.indexOf(entry);
                    this.selecteCatalogs.splice(index, 1);
                }
            });
        }
    }

    savePreferences() {
        let defaultTargetLangs: string[] = [];
        this.tgtLangs.forEach((language: LanguageInterface) => {
            defaultTargetLangs.push(language.code);
        });
        let preferences: Preferences = {
            defaultTheme: (document.getElementById('themeColor') as HTMLSelectElement).value,
            lang: (document.getElementById('appLangSelect') as HTMLSelectElement).value,
            defaultSrcLang: (document.getElementById('srcLangSelect') as HTMLSelectElement).value,
            defaultTgtLang: defaultTargetLangs,
            projectsFolder: (document.getElementById('projectsFolder') as HTMLInputElement).value,
            memoriesFolder: (document.getElementById('memoriesFolder') as HTMLInputElement).value,
            srxFile: (document.getElementById('srxFile') as HTMLInputElement).value,
            translateComments: (document.getElementById('xmlComments') as HTMLInputElement).checked
        };
        this.electron.ipcRenderer.send('save-preferences', preferences);
    }

    setPreferences(arg: Preferences) {
        (document.getElementById('projectsFolder') as HTMLInputElement).value = arg.projectsFolder;
        (document.getElementById('memoriesFolder') as HTMLInputElement).value = arg.memoriesFolder;
        (document.getElementById('srxFile') as HTMLInputElement).value = arg.srxFile;
        (document.getElementById('appLangSelect') as HTMLSelectElement).value = arg.lang;
        (document.getElementById('themeColor') as HTMLSelectElement).value = arg.defaultTheme;
        (document.getElementById('xmlComments') as HTMLInputElement).checked = arg.translateComments;
    }

    removeTargetLanguages(language: string): void {
        let lang: LanguageInterface[] = this.tgtLangs.filter((lang: LanguageInterface) => {
            return lang.code === language;
        });
        let index: number = this.tgtLangs.indexOf(lang[0]);
        this.tgtLangs.splice(index, 1);
        this.displayTargetLanguages();
    }

    setLanguages(languages: LanguageInterface[]): void {
        let select: HTMLSelectElement = document.getElementById('srcLangSelect') as HTMLSelectElement;
        for (let language of languages) {
            let option: HTMLOptionElement = document.createElement('option');
            option.value = language.code;
            option.textContent = language.description;
            select.appendChild(option);
        }
        this.electron.ipcRenderer.send('get-default-languages');
    }

    setDefaultLanguages(arg: { srcLang: LanguageInterface, tgtLangs: LanguageInterface[], removeText: string }): void {
        (document.getElementById('srcLangSelect') as HTMLSelectElement).value = arg.srcLang.code;
        this.tgtLangs = arg.tgtLangs;
        this.removeText = arg.removeText;
        this.displayTargetLanguages();
    }

    displayTargetLanguages(): void {
        this.tgtLangs.sort((a: LanguageInterface, b: LanguageInterface) => {
            return a.description.localeCompare(b.description, document.documentElement.lang);
        });
        let targetLanguages: HTMLTableSectionElement = document.getElementById('targetLanguages') as HTMLTableSectionElement;
        targetLanguages.innerHTML = '';
        for (let language of this.tgtLangs) {
            let row: HTMLTableRowElement = document.createElement('tr');
            targetLanguages.appendChild(row);

            let cell = document.createElement('td');
            cell.style.width = '24px';
            cell.classList.add('middle');
            cell.classList.add('center');

            let remove: HTMLAnchorElement = document.createElement('a');
            remove.classList.add('icon');
            remove.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"><path d="m400-325 80-80 80 80 51-51-80-80 80-80-51-51-80 80-80-80-51 51 80 80-80 80 51 51Zm-88 181q-29.7 0-50.85-21.15Q240-186.3 240-216v-480h-48v-72h192v-48h192v48h192v72h-48v479.57Q720-186 698.85-165T648-144H312Zm336-552H312v480h336v-480Zm-336 0v480-480Z"/></svg>';
            remove.setAttribute('data-title', this.removeText);
            remove.addEventListener('click', () => {
                this.removeTargetLanguages(language.code);
            });
            remove.id = language.code;
            cell.appendChild(remove);
            row.appendChild(cell);

            cell = document.createElement('td');
            cell.classList.add('middle');
            let label: HTMLLabelElement = document.createElement('label');
            label.htmlFor = language.code;
            label.innerText = language.description;
            cell.appendChild(label);
            row.appendChild(cell);
        }
    }

    editFilter(): void {
        if (this.selectedFilters.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'settingsDialog', key: 'selectFilter' });
            return;
        }
        if (this.selectedFilters.length !== 1) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'settingsDialog', key: 'selectOneFilter' });
            return;
        }
        this.electron.ipcRenderer.send('show-edit-filter', this.selectedFilters[0]);
    }

    removeFilters(): void {
        if (this.selectedFilters.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'settingsDialog', key: 'selectFilter' });
            return;
        }
        this.electron.ipcRenderer.send('remove-filters', this.selectedFilters);
    }

    removeCatalog(): void {
        if (this.selecteCatalogs.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'settingsDialog', key: 'selectCatalog' });
            return;
        }
        this.electron.ipcRenderer.send('remove-catalog', this.selecteCatalogs);
    }
}