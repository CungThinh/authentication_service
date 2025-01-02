import { routes } from "./routes";
import { Route, Routes } from "react-router-dom";

function App() {
  return (
    
    <Routes>
      {routes.map((route, index) => (
        <Route key={index} path={route.path} element={<route.page />} />
      ))}
    </Routes>
  );
}

export default App;
