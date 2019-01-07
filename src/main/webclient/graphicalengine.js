const DIRECTIONS = {
    UP: 'up',
    DOWN: 'down',
    LEFT: 'left',
    RIGHT: 'right'
};

class Case {
    static getCaseWidth() {
        return 60
    }
    static getCaseHeight() {
        return 60
    }
    static getDefaultColor() {
        return 'rgba(' + getRandom(0, 255) + ',' + getRandom(0, 255) + ',' + getRandom(0, 255) + ',' + getRandom(1, 1) + ')'
    }

    constructor(x, y, size, direction, color) {
        if (color === undefined) color = Case.getDefaultColor();
        if (size === undefined) size = 1;
        this.x = x;
        this.y = y;
        this.color = color;
        this.direction = direction;
        this.size = size;
    }

    draw(context) {
        context.fillStyle = this.color;
        let xSize = Case.getCaseWidth();
        let ySize = Case.getCaseHeight();
        if (this.direction === DIRECTIONS.UP) {
            ySize = ySize * -this.size
        } else if (this.direction === DIRECTIONS.DOWN) {
            ySize = ySize * this.size
        } else if (this.direction === DIRECTIONS.LEFT) {
            xSize = xSize * -this.size
        } else if (this.direction === DIRECTIONS.RIGHT) {
            xSize = xSize * this.size
        }
        context.fillRect(this.x * Case.getCaseWidth(), this.y * Case.getCaseHeight(), xSize, ySize)
    }
}

function getRandom(min, max) {
    if (max === undefined) {
        max = min;
        min = 0;
    }
    return (Math.random() * (max - min)) + min
}

const world = {
    // Alaska: [1, 2, 2, DIRECTIONS.RIGHT, 'red'],
    Alaska: [0, 3],
    'Territoires du Nord-Ouest': [1, 3, 2, DIRECTIONS.RIGHT],
    Groenland: [3, 3, 2, DIRECTIONS.RIGHT],
    Alberta: [0, 4, 2, DIRECTIONS.RIGHT],
    Ontario: [2, 4, 2, DIRECTIONS.RIGHT],
    Québec: [4, 4],
    "États de l'Est": [1, 5, 2, DIRECTIONS.RIGHT],
    "États de l'Ouest": [3, 5, 2, DIRECTIONS.RIGHT],
    "Amérique centrale": [2, 6, 2, DIRECTIONS.RIGHT],
    Vénézuéla: [4, 9, 2, DIRECTIONS.RIGHT],
    Pérou: [4, 10],
    Brésil: [5, 10],
    Argentine: [4, 11, 2, DIRECTIONS.RIGHT],
    Islande: [7, 3],
    Scandinavie: [8, 3, 3, DIRECTIONS.RIGHT],
    "Grande-Bretagne": [8, 4],
    "Europe du Nord": [9, 4],
    Ukraine: [10, 4],
    "Europe-Occidentale": [8, 5, 2, DIRECTIONS.RIGHT],
    "Europe du Sud": [10, 5],
    "Afrique du Nord": [8, 8, 2, DIRECTIONS.RIGHT],
    Égypte: [10, 8],
    Congo: [8, 9, 2, DIRECTIONS.DOWN],
    "Afrique de l'Est": [9, 9, 2, DIRECTIONS.DOWN],
    "Afrique du Sud": [8, 11, 2, DIRECTIONS.RIGHT],
    "Madagascar": [10, 10, 2, DIRECTIONS.DOWN],
    "Oural": [13, 3, 3, DIRECTIONS.RIGHT],
    Afghanistan: [13, 4, 2, DIRECTIONS.RIGHT],
    "Moyen-Orient": [13, 5],
    Inde: [14, 5, 2, DIRECTIONS.RIGHT],
    Siam: [16, 5],
    Chine: [15, 4, 3, DIRECTIONS.RIGHT],
    Sibérie: [16, 1, 3, DIRECTIONS.DOWN],
    Mongolie: [17, 2, 2, DIRECTIONS.DOWN],
    Japon: [18, 3],
    Kamchatka: [18, 1, 2, DIRECTIONS.DOWN],
    Yakoutie: [16, 0, 3, DIRECTIONS.RIGHT],
    Tchita: [17, 1],
    Indonésie: [13, 8],
    "Nouvelle-Guinée": [14, 8, 2, DIRECTIONS.RIGHT],
    "Australie-Occidentale": [13, 9, 2, DIRECTIONS.RIGHT],
    "Australie-Orientale": [15, 9],
};

window.onload = () => {

    const canvasVirtualWidth = 20;
    const canvasVirtualHeight = 15;
    const canvas = document.createElement('canvas');
    document.body.appendChild(canvas);
    canvas.style.border = '1px solid';
    canvas.style.backgroundColor = 'black';
    canvas.width = canvasVirtualWidth * Case.getCaseWidth();
    canvas.height = canvasVirtualHeight * Case.getCaseHeight();
    const context = canvas.getContext('2d');
    for (const territory in world) {
        // x, y, size, direction, color
        new Case(world[territory][0], world[territory][1], world[territory][2], world[territory][3], world[territory][4]).draw(context)
    }

};
