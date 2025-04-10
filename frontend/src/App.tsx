import React, {useEffect, useState, useRef} from 'react';
import './App.css';

interface Task {
    id: number;
    title: string;
    completed: boolean;
    }

const API_URL = 'http://localhost:8080/tasks';
const WS_URL = 'ws://localhost:8080/ws/tasks';

const App: React.FC = () => {
    const [tasks, setTasks] = useState<Task[]>([]);
    const [title, setTitle] = useState('');
    const wsRef = useRef<WebSocket | null>(null);

    //pobierz zadania przy załadowaniu
    useEffect(() => {
        if (!wsRef.current){
            const ws = new WebSocket(WS_URL);
            ws.onopen = () => console.log('Websocket connected');
            ws.onmessage = (event) => {
                console.log('WebSocket message reveived: ', event.data);
                const data = JSON.parse(event.data);
                if (data.event === 'taskUpdated'){
                    console.log('Task updated, fetching tasks');
                    fetchTasks();
                }
            };
        ws.onerror = (error) => console.error('Websocket error:', error);
        ws.onclose = () => console.log('WebSocket closed');
        }

        fetchTasks();
        return () => {
            if (wsRef.current) {
                wsRef.current.close();
            }
        };
    },[]);

    const fetchTasks = async () =>{
        try {
            const response = await fetch(API_URL);
            const data = await response.json();
            setTasks(data);
        } catch(error) {
                console.error('Błąd przy pobieraniu zadań: ', error)
        }
    };

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
        } catch (error){
                console.error('Błąd przy dodawaniu zadania:', error);
        }
    };

    const handleToggleComplete = async (id: number, completed: boolean) => {
        try {
            await fetch(`${API_URL}/${id}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json'},
                body: JSON.stringify({ completed: !completed }),
            });
        } catch (error){
            console.error('Błąd przy aktualizacji zadania: ',error)
        }
    }


    return(
    <div className="app">
        <h1>Lista Zadań</h1>
        <form onSubmit={handleSubmit}>
        <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Wpisz zadanie"
            required
        />
        <button type="submit">Dodaj</button>
        </form>
        <ul>
        {tasks.map((task)=>(
            <li key={task.id}>
                <input
                    type="checkbox"
                    checked={task.completed}
                    onChange={() => handleToggleComplete(task.id, task.completed)}
                />
            {task.title} (Zakończone: {task.completed ? 'Tak' : 'Nie'})
            </li>
            ))}
        </ul>
    </div>
    );
};

export default App;
