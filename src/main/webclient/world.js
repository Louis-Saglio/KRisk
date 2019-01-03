function getRandom(min, max) {
    console.log(min, max);
    if (max === undefined) {
        max = min;
        min = 0;
    }
    return (Math.random() * (max - min)) + min
}


const world = {
    territories: {
        '0': {
            '3': 'Alaska',
            '4': 'Alberta',
        },
        '1': {
            '3': "Territoires du Nord-Ouest",
            '4': 'Alberta',
            '5': 'États de l\'Ouest',
        },
        '2': {
            '3': "Territoires du Nord-Ouest",
            '4': 'Ontario',
            '5': 'États de l\'Ouest',
            '6': "Amérique centrale"
        },
        '3': {
            '3': "Groenland",
            '4': 'Ontario',
            '5': 'États de l\'Est',
            "6": "Amérique centrale"
        },
        '4': {
            '3': "Groenland",
            '4': 'Québec',
            '5': 'États de l\'Est',
            '8': 'Vénézuéla',
            '9': 'Pérou',
            '10': 'Argentine'
        },
        '5': {
            '8': 'Vénézuéla',
            '9': 'Brésil',
            '10': 'Argentine'
        },
        '7': {
            '3': 'Islande',
        },
        '8': {
            '3': "Scandinavie",
            '4': 'Grande-Bretagne',
            '5': "Europe-Occidentale",
            '8': 'Afrique du Nord',
            '9': 'Congo',
            '10': 'Congo',
            '11': 'Afrique du Sud'
        },
        '9': {
            '3': "Scandinavie",
            '4': 'Europe du Nord',
            '5': "Europe-Occidentale",
            '8': 'Afrique du Nord',
            '9': 'Afrique de l\'Est',
            '10': 'Afrique de l\'Est',
            '11': 'Afrique du Sud'
        },
        '10': {
            '3': "Scandinavie",
            '4': 'Ukraine',
            '5': "Europe du Sud",
            '8': 'Egypte',
            '10': 'Madagascar',
            '11': 'Madagascar'
        },
        '13': {
            '3': 'Oural',
            '4': 'Afghanistan',
            '5': 'Moyen-Orient',
            '8': 'Indonésie',
            '9': 'Australie occidentale',
        },
        '14': {
            '3': 'Oural',
            '4': 'Afghanistan',
            '5': 'Inde',
            '8': 'Nouvelle-Guinée',
            '9': 'Australie occidentale',
        },
        '15': {
            '3': 'Oural',
            '4': 'Chine',
            '5': 'Inde',
            '8': 'Nouvelle-Guinée',
            '9': 'Australie orientale',
        },
        '16': {
            '0': 'Yakoutie',
            '1': 'Sibérie',
            '2': 'Sibérie',
            '3': 'Sibérie',
            '4': 'Chine',
            '5': 'Siam',
        },
        '17': {
            '0': 'Yakoutie',
            '1': 'Tchita',
            '2': 'Mongolie',
            '3': 'Mongolie',
            '4': 'Chine',
        },
        '18': {
            '0': 'Yakoutie',
            '1': 'Kamchatka',
            '2': 'Kamchatka',
            '3': 'Japon',
        },
    },
    colNbr: 19,
    rowNbr: 12,
    cellWidth: '5%',
    cellHeight: '50px',

    display : function () {
        const colors = {};
        const table = document.createElement('table');
        document.body.appendChild(table);
        table.style.borderCollapse = 'collapse';
        table.style.width = '100%';
        for (let i = 0; i < this.rowNbr; i++) {
            const row = document.createElement('tr');
            table.appendChild(row);
            for (let j = 0; j < this.colNbr; j++) {
                const cell = document.createElement('td');
                row.appendChild(cell);
                cell.style.width = this.cellWidth;
                cell.style.height = this.cellHeight;
                if (this.territories.hasOwnProperty(j) && this.territories[j].hasOwnProperty(i)) {
                    const territory = this.territories[j][i];
                    console.log(territory);
                    if (!colors.hasOwnProperty(territory)) {
                        colors[territory] = 'rgba(' + getRandom(255) + ',' + getRandom(255) + ',' + getRandom(255) + ',' + getRandom(0.5, 1) + ')';
                    }
                    cell.style.backgroundColor = colors[territory]
                } else {
                    cell.style.backgroundColor = "transparent"
                }
            }
        }
    }
};

window.onload = () => {
    world.display()
};
