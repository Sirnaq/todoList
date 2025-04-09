import React, {useEffect, useState} from 'react';
import './App.css';

interface Task {
    id: number;
    title: string;
    completed: boolean;
    }
const API_URL = 'http://localhost:8080/tasks';

const App: React.FC = ()=>{
    const [tasks, setTasks] = useState<Task[]>([]);
    const [title, setTitle] = useState('');

    //pobierz zadania przy załadowaniu
    useEffect(()=>{
        fetchTasks();
        },[])

    const fetchTasks = async () =>{
        try {
            const response = await fetch(API_URL);
            const data = await response.json();
            setTasks(data);
            } catch(error){
                console.error('Błąd przy pobieraniu zadań: ', error)
            }
    }

    const handleSubmit = async (e: React.FormEvent) =>{
        e.preventDefault();
        const task = {id: Date.now(), title, completed: false};

        try {
            await fetch(API_URL, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(task),
                });
            setTitle(''); // Wyczyść pole
            setTimeout(fetchTasks, 1000); // Odśwież po 1s (RabbitMQ)
        } catch (error){
                console.error('Błąd przy dodawaniu zadania:', error);
        }
    }

    return(
    <div className="app">
        <h1>Lista Zadań</h1>
        <form onSubmit={handleSubmit}>
        <input
            type="text"
            value={title}
            onChange={(e)=>setTitle(e.target.value)}
            placeholder="Wpisz zadanie"
            required
        />
        <button type="submit">Dodaj</button>
        </form>
        <ul>
        {tasks.map((task)=>(
            <li key={task.id}>
            {task.title} (Zakończone: {task.completed ? 'Task' : 'Nie'})
            </li>
            ))}
        </ul>
    </div>
    );
};

export default App;
