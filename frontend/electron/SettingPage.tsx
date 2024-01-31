import { Slider } from "@/components/ui/slider";
import axios from "axios";

declare global {
  interface Window {
    electron: typeof import("electron");
  }
}

const electron = window.electron;

export default function SettingPage() {
  const handleClick = () => {
    electron.ipcRenderer.send("button-clicked", "hi");
    console.log("React button clicked");
  };

  const summonerName = async () => {
    let sname = "안동민";
    const response = await axios({
      method: "get",
      url: `https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-name/${sname}?api_key=${import.meta.env.VITE_Riot_API_KEY}`,
    });
    console.log(response.data);
  };
  const handleTransparencyChange = (value : any = 100) => {
    // 투명도 값이 변경될 때의 로직을 여기에 추가
    document.body.style.backgroundColor = `rgba(255, 255, 255, ${value / 100})`;
    console.log("Transparency changed:", value);
  };

  return (
    <div>
      <div >
        <Slider onValueChange={handleTransparencyChange} className="w-40"/>
      </div>
      <br />
      <br />
      <button onClick={handleClick}>Click Me</button>
      <br />
      <br />
      <button onClick={summonerName}>Click Me</button>
      <br />
    </div>
  );
}
