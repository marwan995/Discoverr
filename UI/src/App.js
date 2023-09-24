import "./App.css";
import SearchContextProvider from "./context/SearchContextProvider";
import Home from "./pages/Home";
import Results from "./pages/Ruslts";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
function App() {
  return (
    <Router>
      <SearchContextProvider>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/results" element={<Results />} />
        </Routes>
      </SearchContextProvider>
    </Router>
  );
}

export default App;
