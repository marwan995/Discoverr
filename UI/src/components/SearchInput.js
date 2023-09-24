import React, { useRef } from "react";
import TextField from "@mui/material/TextField";
import Autocomplete from "@mui/material/Autocomplete";
import { useState, useEffect } from "react";
import axios from "axios";
import InputAdornment from "@mui/material/InputAdornment";
import PhotoCameraIcon from "@mui/icons-material/PhotoCamera";
import MicIcon from "@mui/icons-material/Mic";
import MicNoneIcon from "@mui/icons-material/MicNone";
import SpeechRecognition, {
  useSpeechRecognition,
} from "react-speech-recognition";

export default function SearchInput({
  setSearchQuery,
  handleImageChange,
  search,
}) {
  const [textFieldValue, setTextFieldValue] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const inputRef = useRef(null);

  const handleTextFieldChange = (event) => {
    setTextFieldValue(event.target.value);
    setSearchQuery(event.target.value);
  };
  const handleAutocompleteChange = (event, value) => {
    setTextFieldValue(value);
    setSearchQuery(value);
  };
  useEffect(() => {
    axios.get("http://localhost:8080/Query").then((res) => {
      setSearchResults(res.data);
    });
  }, []);

  const {
    transcript,
    listening,
    resetTranscript,
    browserSupportsSpeechRecognition,
  } = useSpeechRecognition();

  //       <button onClick={resetTranscript}>Reset</button>

  useEffect(() => {
    setTextFieldValue(transcript);
    setSearchQuery(transcript);
  }, [transcript]);

  useEffect(() => {
    if (listening || textFieldValue === "") return;
    search(textFieldValue);
    resetTranscript();
  }, [listening]);

  return (
    <Autocomplete
      className="inputContainer"
      sx={{ borderColor: "white" }}
      freeSolo
      onChange={handleAutocompleteChange}
      id="free-solo-2-demo"
      disableClearable={true}
      clearIcon={null}
      value={textFieldValue}
      options={searchResults}
      renderInput={(params) => (
        <TextField
          className="input"
          onChange={handleTextFieldChange}
          value={textFieldValue}
          {...params}
          label="Search...."
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <InputAdornment position="end">
                {listening ? (
                  <MicIcon
                    style={{
                      fill: "#ffffff",
                      cursor: "pointer",
                      marginBottom: 40,
                      marginLeft: "auto",
                      marginRight: 10,
                    }}
                    onClick={() => SpeechRecognition.stopListening()}
                  />
                ) : (
                  <MicNoneIcon
                    style={{
                      fill: "#ffffff",
                      cursor: "pointer",
                      marginBottom: 40,
                      marginLeft: "auto",
                      marginRight: 10,
                    }}
                    onClick={() => SpeechRecognition.startListening()}
                  />
                )}
                <PhotoCameraIcon
                  style={{
                    fill: "#ffffff",
                    cursor: "pointer",
                    marginBottom: 40,
                    marginRight: 10,
                  }}
                  onClick={() => inputRef.current.click()}
                />
                <input
                  type="file"
                  id="image-upload"
                  ref={inputRef}
                  onChange={handleImageChange}
                  style={{ display: "none" }}
                />
              </InputAdornment>
            ),
            type: "search",
          }}
          required
        />
      )}
    />
  );
}
