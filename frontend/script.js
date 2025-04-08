const API_URL = 'http://localhost:8080/tasks';

// Najpierw definiujemy funkcję
async function fetchTasks() {
    try {
        const response = await fetch(API_URL);
        const tasks = await response.json();
        const taskList = document.getElementById('taskList');
        taskList.innerHTML = '';
        tasks.forEach(task => {
            const li = document.createElement('li');
            li.textContent = `${task.title} (Zakończone: ${task.completed ? 'Tak' : 'Nie'})`;
            taskList.appendChild(li);
        });
    } catch (error) {
        console.error('Błąd przy pobieraniu zadań:', error);
    }
}

// Potem używamy jej w listenerze
document.addEventListener('DOMContentLoaded', fetchTasks);

document.getElementById('taskForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const title = document.getElementById('taskTitle').value;
    const task = { id: Date.now(), title, completed: false };

    try {
        await fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(task)
        });
        document.getElementById('taskTitle').value = '';
        setTimeout(fetchTasks, 1000);
    } catch (error) {
        console.error('Błąd przy dodawaniu zadania:', error);
    }
});